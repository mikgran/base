package mg.angular.rest;

import static java.lang.String.format;
import static mg.util.Common.hasContent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.angular.db.Contact;
import mg.angular.db.ContactService;
import mg.util.db.persist.DBValidityException;

@Path("/contacts")
public class ContactResource {

    // TOCONSIDER: exit program on major failure and-or reporting and-or monitoring
    // TOIMPROVE: give a proper REST API error message in case of a failure.
    // TOIMPROVE: return some entity from removes
    // TOIMPROVE: Generic service class map (Clazz.class -> MyService.class)
    //              path: api3/{clazzName}/{id} -> @Path("{clazzName}") + @PathParam("clazzName") String clazzName
    //              serviceMap.get(clazzName).<operationNamePlusParameters> OR inject based on the Clazz.class
    // XXX add rest end-to-end tests

    private static final String JSON_EMPTY = "{}";
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private ContactService contactService = new ContactService();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts(@DefaultValue("") @QueryParam("fields") String requestedFields,
        @DefaultValue("") @QueryParam("sort") QuerySortParameters querySortParameters) {

        logger.info("getAllContacts(fields: " + requestedFields + ", sort: " + querySortParameters.getQuerySortParameters() + ")");

        List<Contact> contacts = contactService.findAll(querySortParameters.getQuerySortParameters()); // TOCONSIDER: change DB query to fetch only requested fields.

        String contactJson = contactService.getJson(requestedFields, contacts);

        return getResponse(hasContent(contacts), contactJson);
    }

    @GET
    @Path("{contactId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getContact(@PathParam("contactId") Long contactId,
        @QueryParam("fields") String requestedFields) {

        logger.info("getContact(" + contactId + ")");
        Response response = null;
        String contactJson = "";

        Contact contact = contactService.find(contactId);
        boolean isContactFound = (contact != null);

        if (isContactFound) {
            contactJson = contactService.getJson(requestedFields, contact);
        }
        response = getResponse(isContactFound, contactJson);

//        response = Response.status(Response.Status.BAD_REQUEST)
//                           .entity("Please provide a valid {id}.")
//                           .build();

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

    private Response getResponse(boolean hasContent, String contactJson) {
        Response response;
        if (!contactJson.matches(".*[a-zA-Z]+.*") && hasContent) { // case filtered all fields down to: [{},{}], user provided funky query. TOCONSIDER: return an InvalidRequest

            response = getResponse(Response.Status.NO_CONTENT, JSON_EMPTY);
        } else if (hasContent) {

            response = getResponse(Response.Status.OK, contactJson);
        } else {

            response = getResponse(Response.Status.NOT_FOUND, JSON_EMPTY);
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
