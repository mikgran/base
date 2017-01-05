package mg.angular.rest;

import static java.lang.String.format;
import static mg.util.Common.hasContent;
import static mg.util.Common.instancesOf;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.angular.db.Contact;
import mg.angular.db.ContactService;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;
import mg.util.db.persist.Persistable;
import mg.util.rest.QuerySortParameter;
import mg.util.rest.RestUtil;

@Path("/contacts")
public class ContactResource {

    // TOCONSIDER: exit program on major failure and-or reporting and-or monitoring
    // TOIMPROVE: give a proper REST API error message in case of a failure.
    // TOIMPROVE: return some entity from removes

    private static final String ERROR_WHILE_TRYING_TO_FIND_ALL_CONTACTS = "Error while trying to findAll contacts: ";
    private static final String JSON_EMPTY = "{}";
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts(@QueryParam("fields") String requestedFields,
        @QueryParam("sort") String sorts) {

        logger.info("getting all contacts");
        Response response = null;

        try {
            List<QuerySortParameter> sortParameters = RestUtil.parseQuerySortParams(sorts);

            ContactService contactService = new ContactService();
            List<Contact> contacts = contactService.findAll(sortParameters); // TOCONSIDER: change DB query to fetch only requested fields.

            String contactJson = getJson(requestedFields, contacts);

            response = getResponse(hasContent(contacts), contactJson);

        } catch (SQLException | DBValidityException | DBMappingException | ClassNotFoundException e) {

            logger.error(ERROR_WHILE_TRYING_TO_FIND_ALL_CONTACTS, e);
            response = getResponseForInternalServerError();

        } catch (JsonProcessingException e) {

            logger.error(ERROR_WHILE_TRYING_TO_FIND_ALL_CONTACTS, e);
            response = getResponseForInternalServerError();
        }
        return response;
    }

    @GET
    @Path("{contactId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getContact(@PathParam("contactId") Long contactId,
        @QueryParam("fields") String requestedFields) {

        logger.info("getting contact for id: " + contactId);
        Response response = null;
        String contactJson = "";

        try {
            if (hasContent(contactId)) {

                ContactService contactService = new ContactService();
                Contact contact = contactService.find(contactId);
                boolean isContactFound = (contact != null);

                if (isContactFound) {
                    contactJson = getJson(requestedFields, contact);
                }
                response = getResponse(isContactFound, contactJson);

            } else {
                response = Response.status(Response.Status.BAD_REQUEST)
                                   .entity("Please provide a valid {id}.")
                                   .build();
            }

        } catch (SQLException | DBValidityException | DBMappingException | ClassNotFoundException e) {

            logger.error(ERROR_WHILE_TRYING_TO_FIND_ALL_CONTACTS, e);
            response = getResponseForInternalServerError();

        } catch (JsonProcessingException e) {

            logger.error(ERROR_WHILE_TRYING_TO_FIND_ALL_CONTACTS, e);
            response = getResponseForInternalServerError();
        }

        return response;
    }

    @DELETE
    @Path("{contactId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response removeContact(@PathParam("contactId") Long contactId) {

        logger.info(format("removing contact: %s", contactId));
        Response response = null;

        ContactService contactService = new ContactService();

        try {

            if (hasContent(contactId)) {

                contactService.remove(contactId);
                response = Response.status(Response.Status.OK)
                                   .build();
            }

        } catch (IllegalArgumentException | ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to remove a contact: " + contactId, e);
            response = getResponseForInternalServerError();
        }

        return response;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response saveContact(String s) {

        logger.info(format("saving contact: %s", s));
        Response response = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Contact contact = objectMapper.readValue(s, Contact.class);

            ContactService contactService = new ContactService();
            contactService.saveContact(contact);

            response = Response.status(Response.Status.CREATED)
                               .build();

        } catch (ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to save a contact to DB.", e);

            response = getResponseForInternalServerError();

        } catch (IOException e) {

            String message = "Unable to parse json into Contact.class.";
            logger.error(message, e);

            response = Response.status(Response.Status.BAD_REQUEST)
                               .entity(message)
                               .build();
        }

        return response;
    }

    private String getFilterName(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredAnnotations())
                     .flatMap(instancesOf(JsonFilter.class))
                     .map(JsonFilter::value)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Class: " + clazz + " does not have JsonFilter(\"<name>\")"));
    }

    private String getJson(String requestedFields, Contact contact) throws JsonProcessingException {

        // extract to own method.
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(getNamedFiltersForClass(requestedFields, getFilterName(Contact.class)));
        String contactJson = writer.writeValueAsString(contact);
        return contactJson;
    }

    private String getJson(String requestedFields, List<Contact> contacts) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(getNamedFiltersForClass(requestedFields, getFilterName(Contact.class)));
        String contactJson = writer.writeValueAsString(contacts);
        return contactJson;
    }

    private SimpleFilterProvider getNamedFiltersForClass(String requestedFields, String filterId) {

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

    private Response getResponse(boolean hasContent, String contactJson) {
        Response response;
        if (!contactJson.matches(".*[a-zA-Z]+.*")) { // case [{},{}], user provided funky query. TOCONSIDER: return an InvalidRequest

            response = getResponse(Response.Status.NO_CONTENT, JSON_EMPTY);
        } else if (hasContent) {

            response = getResponse(Response.Status.OK, contactJson);
        } else {

            response = getResponse(Response.Status.NO_CONTENT, JSON_EMPTY);
        }
        return response;
    }

    private Response getResponse(Status status, String json) {
        return Response.status(status)
                       .entity(json)
                       .build();
    }

    private Response getResponseForInternalServerError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .build();
    }
}
