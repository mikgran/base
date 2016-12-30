package mg.angular.rest;

import static java.lang.String.format;
import static mg.util.Common.hasContent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
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
    // TOIMPROVE: sorting, listing
    // TOIMPROVE: better connection handling.

    // XXX add: sorting
    // XXX REST: remove/delete

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

            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(getContactFilters(requestedFields));
            String contactJson = writer.writeValueAsString(contacts);

            if (!contactJson.matches(".*[a-zA-Z]+.*")) { // case [{},{}], user provided funky query. TOCONSIDER: return an InvalidRequest

                response = getResponse(Response.Status.NO_CONTENT, JSON_EMPTY);

            } else if (hasContent(contacts)) {

                response = getResponse(Response.Status.OK, contactJson);

            } else {

                response = getResponse(Response.Status.NO_CONTENT, JSON_EMPTY);
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

    @GET
    @Path("{contactId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getContact(@PathParam("contactId") Long contactId) {

        logger.info("getting contact for id: " + contactId);

        try {

        } catch (Exception e) {

        }

        return Response.status(Response.Status.NO_CONTENT)
                       .build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response saveContact(String s) {

        logger.info(format("got post: %s", s));
        Response response = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Contact contact = objectMapper.readValue(s, Contact.class);

            ContactService contactService = new ContactService();
            Contact savedContact = contactService.saveContact(contact);

            response = Response.status(Response.Status.CREATED)
                               .entity(savedContact)
                               .build();

        } catch (ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to save a contact to DB.", e);

            response = getResponseForInternalServerError();

        } catch (IOException e) {

            String message = "Unable to parse json into Contact.class.";
            logger.error(message, e);

            response = Response.status(Response.Status.BAD_REQUEST)
                               .build();
        }

        return response;
    }

    private SimpleFilterProvider getContactFilters(String requestedFields) {

        SimpleBeanPropertyFilter contactFilters = null;

        if (hasContent(requestedFields)) {

            // case all requested fields.
            contactFilters = SimpleBeanPropertyFilter.filterOutAllExcept(requestedFields.split(","));

        } else {
            // case all but Persistable fields:
            String[] excludeFields = Persistable.getJsonExcludeFields();
            contactFilters = SimpleBeanPropertyFilter.serializeAllExcept(excludeFields);
        }

        return new SimpleFilterProvider().addFilter("contactFilter", contactFilters);
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
