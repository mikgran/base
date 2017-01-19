package mg.angular.rest;

import static mg.util.Common.hasContent;
import static mg.util.rest.QuerySortParameterType.SORT_ASCENDING;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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

import mg.angular.db.Contact;
import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.db.persist.DB;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
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

    // XXX test coverage
    public Contact find(Long id) {

        validateId(id);

        try (Connection connection = dbConfig.getConnection()) {

            Contact contact = null;
            DB db = new DB(connection);
            contact = db.findById(new Contact().setId(id));

            validateContent(contact);

            return contact;

        } catch (ClassNotFoundException | SQLException | DBValidityException | DBMappingException e) {

            logger.error("Unable to find contact with id: " + id, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // XXX test coverage
    public List<Contact> findAll() {
        return this.findAll(Collections.emptyList());
    }

    // XXX test coverage
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

    // XXX test coverage
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

    // XXX test coverage
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

    // XXX test coverage
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

    // XXX test coverage
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

    // XXX test coverage
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

    // TOIMPROVE: missing case faulty sort parameters provided: throw new WEA for bad query
    private void assignQueryParameters(MultivaluedMap<String, String> queryParameters, Contact contact) {

        String sortParameters = queryParameters.containsKey("sort") ? queryParameters.getFirst("sort") : "";
        QuerySortParameters querySortParameters = new QuerySortParameters(sortParameters);

        assignSortParameters(querySortParameters, contact);
        assingFreeTextSearchParameters(querySortParameters, contact);
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

    private void assingFreeTextSearchParameters(QuerySortParameters querySortParameters, Contact contact) {

        // 1. option: sort parameters 1,2,3,4... match q parameters 1,2,3,4
        // detonate the search if sort.size == 0 and q.size > 0 WEA: bad query

        // 2. option: searchTerm and q needed both for free search
        // missing searchTerm or if searchTerm.size <> q.size detonates the search with WEA: bad query



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

    private void validateContent(Contact contact) {
        if (contact == null) {
            throw new WebApplicationException(Response.Status.NO_CONTENT);
        }
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

}
