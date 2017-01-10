package mg.angular.rest;

import static mg.util.Common.hasContent;
import static mg.util.rest.QuerySortParameterType.SORT_ASCENDING;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private DBConfig dbConfig;
    private ObjectMapper mapper;

    public ContactService() {

        try {
            PropertyConfigurator.configure("log4j.properties");
            dbConfig = new DBConfig(new Config());

        } catch (IOException e) {

            // client can not recover from this exception, and the server should shut down:
            String msg = "Unable to initialize ContactService: ";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
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
                SimpleFilterProvider filterProvider = new SimpleFilterProvider();
                filterProvider.setFailOnUnknownId(false);
                ObjectWriter writer = mapper.writer(filterProvider);

                json = writer.writeValueAsString(o);
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

        try {
            Contact contact = new Contact();
            contact.setConnectionAndDB(dbConfig.getConnection());
            contact.setId(id);
            contact.remove();

        } catch (IllegalArgumentException | ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to remove a contact with id: " + id, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // XXX test coverage
    public Contact saveContact(Contact contact) {

        try {
            contact.setConnectionAndDB(dbConfig.getConnection());
            contact.save();

        } catch (IllegalArgumentException | ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to save a contact to DB.", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return contact;
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
