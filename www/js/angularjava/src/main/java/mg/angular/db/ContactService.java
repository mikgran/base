package mg.angular.db;

import static mg.util.Common.hasContent;
import static mg.util.Common.instancesOf;
import static mg.util.rest.QuerySortParameterType.SORT_ASCENDING;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

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
    }

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

    public List<Contact> findAll() {
        return this.findAll(Collections.emptyList());
    }

    public List<Contact> findAll(List<QuerySortParameter> querySortParameters) {

        try (Connection connection = dbConfig.getConnection()) {

            Contact contact = new Contact(connection);

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
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(getNamedFilterForClass(getFilterName(Contact.class), requestedFields));
            String contactJson = writer.writeValueAsString(o);

            if (!contactJson.matches(".*[a-zA-Z]+.*")) {
                throw new WebApplicationException("Request filtered out all fields.", Response.Status.BAD_REQUEST);
            }

            return contactJson;
        } catch (JsonProcessingException e) {

            logger.error("Unable to write json for object: " + o);
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

    // TOCONSIDER: return removed contact/affected object count from the database?
    public void remove(Long id) {

        validateId(id);

        try {
            Contact contact = new Contact();
            contact.setConnectionAndDB(dbConfig.getConnection());
            contact.setId(id);
            contact.remove();

        } catch (IllegalArgumentException | ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to remove a contact: " + id, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

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

    private String getFilterName(Class<?> clazz) {

        return Arrays.stream(clazz.getDeclaredAnnotations())
                     .flatMap(instancesOf(JsonFilter.class))
                     .map(JsonFilter::value)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Class: " + clazz + " does not have JsonFilter(\"<name>\")"));
    }

    private SimpleFilterProvider getNamedFilterForClass(String filterId, String requestedFields) {

        SimpleBeanPropertyFilter persistableFilters = null;

        if (hasContent(requestedFields)) {

            // case all requested fields.
            persistableFilters = SimpleBeanPropertyFilter.filterOutAllExcept(requestedFields.split(","));

        } else {
            // case all but Persistable fields:
            String[] excludeFields = Persistable.getJsonExcludeFields();
            persistableFilters = SimpleBeanPropertyFilter.serializeAllExcept(excludeFields);
        }

        return new SimpleFilterProvider().addFilter(filterId, persistableFilters);
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
            throw new WebApplicationException("Id provided was 0", Response.Status.BAD_REQUEST);
        }
    }

}
