package mg.restgen.rest;

import static java.util.stream.Stream.of;
import static mg.util.Common.hasContent;
import static mg.util.Common.splitToStream;
import static mg.util.Common.zip;
import static mg.util.rest.QuerySortParameterType.SORT_ASCENDING;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.db.Contact;
import mg.util.Common;
import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.db.persist.DB;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
//import mg.util.db.persist.support.Contact;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.rest.QuerySortParameter;

public class ContactService {

    private DBConfig dbConfig;
    private SimpleFilterProvider defaultFilterProvider;
    private ObjectWriter defaultWriter;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private ObjectMapper mapper;

    public ContactService() {

        initDBConfig();
        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
    }

    public Contact find(Long id) {

        validateId(id);

        try (Connection connection = dbConfig.getConnection()) {

            Contact contact = null;
            DB db = new DB(connection);
            contact = db.findById(new Contact().setId(id));

            if (contact == null) {
                throw new WebApplicationException(Response.Status.NO_CONTENT);
            }

            return contact;

        } catch (ClassNotFoundException | SQLException | DBValidityException | DBMappingException e) {

            logger.error("Unable to find contact with id: " + id, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Contact> findAll() {
        return this.findAll(Collections.emptyList());
    }

    public List<Contact> findAll(List<QuerySortParameter> querySortParameters) {

        try (Connection connection = dbConfig.getConnection()) {

            Contact contact = new Contact(connection);

            // TOIMPROVE: missing case faulty sort parameters provided: throw new WEA for bad query
            querySortParameters.stream()
                               .forEach(sortParameter -> {
                                   contact.field(sortParameter.getParameter());
                                   if (sortParameter.getType() == SORT_ASCENDING) {
                                       contact.orderByAscending();
                                   } else {
                                       contact.orderByDescending();
                                   }
                               });

            List<Contact> allContacts = contact.findAll();

            validateContent(allContacts);

            return allContacts;

        } catch (ClassNotFoundException | SQLException | DBValidityException | DBMappingException e) {

            logger.error("Error while trying to findAll contacts: ", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Contact> findAll(MultivaluedMap<String, String> queryParameters) {

        try (Connection connection = dbConfig.getConnection()) {

            Contact contact = new Contact(connection);

            assignQueryParameters(queryParameters, contact);

            List<Contact> allContacts = contact.findAll();

            validateContent(allContacts);

            return allContacts;

        } catch (ClassNotFoundException | SQLException | DBValidityException | DBMappingException e) {

            logger.error("Error while trying to findAll contacts: ", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public String getJson(String requestedFields, Object o) {

        logger.info("requestedFields: " + requestedFields + " object: " + o);

        try {
            String json = "";

            if (hasContent(requestedFields)) {

                SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept(requestedFields.split(","));
                SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter("defaultFilter", filter);
                ObjectWriter writer = mapper.writer(filterProvider);

                json = writer.writeValueAsString(o);

                // case funky query -> [{}, {}], user filtered everything out: // TOCONSIDER: validate against objects fields.
                if (!json.matches(".*[a-zA-Z]+.*")) {
                    throw new WebApplicationException("Request filtered out all fields.", Response.Status.BAD_REQUEST);
                }

            } else {
                json = defaultWriter.writeValueAsString(o);
            }

            return json;

        } catch (JsonProcessingException e) {

            logger.error("Unable to write json for object: " + o, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public <T extends Persistable> T readValue(String json, Class<T> clazz) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, clazz);

        } catch (IOException e) {

            String message = "Unable to parse json into Contact.class.";
            logger.error(message, e);

            throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
        }
    }

    // TOCONSIDER: return removed contact/affected object count from the database?
    public void remove(Long id) {

        validateId(id);

        try (Connection connection = dbConfig.getConnection()) {
            Contact contact = new Contact();
            contact.setConnectionAndDB(connection);
            contact.setId(id);
            contact.remove();

        } catch (IllegalArgumentException | ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to remove a contact with id: " + id, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public Contact saveContact(Contact contact) {

        try (Connection connection = dbConfig.getConnection()) {
            contact.setConnectionAndDB(connection);
            contact.save();

        } catch (IllegalArgumentException | ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to save a contact to DB.", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return contact;
    }

    private void assignQueryParameters(MultivaluedMap<String, String> queryParameters, Contact contact) {

        if (queryParameters.containsKey("sort")) {

            List<String> sortParameterList = queryParameters.get("sort");
            String sortParameters = combineSortParameters(sortParameterList);

            if (!hasContent(sortParameters)) {
                throw new WebApplicationException("sort must contain at least one value", Response.Status.BAD_REQUEST);
            }

            QuerySortParameters querySortParameters = new QuerySortParameters(sortParameters);

            assignSortParameters(querySortParameters, contact);
        }

        if (of("searchTerm", "q").anyMatch(queryParameters::containsKey)) {

            // searchTerm and q needed both for free search
            // missing searchTerm or if searchTerm.size <> q.size detonates the search with WEA: bad query
            List<String> searchTerms = queryParameters.get("searchTerm");
            List<String> q = queryParameters.get("q");

            if (!hasContent(q) || !hasContent(searchTerms)) {
                throw new WebApplicationException("searchTerm and q must contain data", Response.Status.BAD_REQUEST);
            }

            if (searchTerms.size() != q.size()) {
                throw new WebApplicationException("searchTerm and q must have equal number of parameters", Response.Status.BAD_REQUEST);
            }

            assingFreeTextSearchParameters(searchTerms, q, contact);
        }
    }

    private void assignSortParameters(QuerySortParameters querySortParameters, Persistable persistable) {
        querySortParameters.getQuerySortParameters()
                           .stream()
                           .forEach(sortParameter -> {
                               persistable.field(sortParameter.getParameter());
                               if (sortParameter.getType() == SORT_ASCENDING) {
                                   persistable.orderByAscending();
                               } else {
                                   persistable.orderByDescending();
                               }
                           });
    }

    private void assingFreeTextSearchParameters(List<String> searchTerms, List<String> qs, Persistable persistable) {

        // searchTerms searchTerm(1)=fieldName1, searchTerm(2)=fieldName2, fieldName3, to be applied
        // to the q(1)="__Name Test", q(2)="Some Other Name"
        // produces: fieldName1 LIKE "__Name Test" AND fieldName2 LIKE "__Name Test" AND fieldName3 LIKE "Some Other Name"
        try {
            zip(searchTerms, qs, (searchTerm, query) -> {

                return splitToStream(searchTerm, ",").filter(Common::hasContent)
                                                     .map(s -> new SearchTermQuery(s, query));
            })
              .flatMap(o -> o)
              .forEachOrdered((ThrowingConsumer<SearchTermQuery, Exception>) stq -> {

                  persistable.field(stq.searchTerm) // explodes if the caller used a wrong field name
                             .like(stq.query); // TOCONSIDER: replace the detonating calls with a validation
              });

        } catch (Exception e) {
            throw new WebApplicationException("Search terms must correspond to resource fields:" + e.getMessage(), Response.Status.BAD_REQUEST);
        }

    }

    private String combineSortParameters(List<String> sortParameterList) {
        return sortParameterList.stream()
                                .flatMap(list -> Stream.of(list))
                                .filter(Common::hasContent)
                                .map(String::trim)
                                .collect(Collectors.joining(","));
    }

    private void initDBConfig() {
        try {
            PropertyConfigurator.configure("log4j.properties");
            dbConfig = new DBConfig(new Config());

        } catch (IOException e) {

            // client can not recover from this exception, and the server should shut down:
            String msg = "Unable to initialize ContactService: ";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private void initDefaultFilterProvider() {
        defaultFilterProvider = new SimpleFilterProvider();
        defaultFilterProvider.setFailOnUnknownId(false);
    }

    private void initDefaultWriter() {
        defaultWriter = mapper.writer(defaultFilterProvider);
    }

    private void initMapper() {
        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }

    private void validateContent(List<Contact> allContacts) {
        if (allContacts.isEmpty()) {
            throw new WebApplicationException(Response.Status.NO_CONTENT);
        }
    }

    private void validateId(Long id) {
        if (!hasContent(id)) {
            throw new WebApplicationException("Id provided was 0 or null.", Response.Status.BAD_REQUEST);
        }
    }

    private class SearchTermQuery {

        public final String searchTerm;
        public final String query;

        public SearchTermQuery(String searchTerm, String query) {
            this.searchTerm = searchTerm;
            this.query = query;
        }
    }

}
