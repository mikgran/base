package mg.angular.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mg.angular.db.Contact;
import mg.angular.service.ContactService;
import mg.util.Common;

@Path("/contacts")
public class ContactResource {

    // TOCONSIDER: exit program on major failure and-or reporting and-or monitoring
    // TOIMPROVE: give a proper REST API error message in case of a failure.
    // TOIMPROVE: return some entity from removes
    // TOIMPROVE: Generic service class map (Clazz.class -> MyService.class)
    //              path: api3/{clazzName}/{id} -> @Path("{clazzName}") + @PathParam("clazzName") String clazzName
    //              serviceMap.get(clazzName).<operationNamePlusParameters> OR inject based on the Clazz.class

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private ContactService contactService = new ContactService();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts(@Context UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        logger.info("getAllContacts(queryParameters: " + queryParameters + ")");

        List<String> requestedFieldsList = queryParameters.get("fields");
        String requestedFields = Common.splitToStream(requestedFieldsList, ",")
                                       .collect(Collectors.joining(","));

        List<Contact> contacts = contactService.findAll(queryParameters);
        String json = contactService.getJson(requestedFields, contacts);

        return getOkResponse(json);
    }

    @GET
    @Path("{contactId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getContact(@PathParam("contactId") Long contactId,
        @DefaultValue("") @QueryParam("fields") String requestedFields) {

        logger.info("getContact(" + contactId + ")");

        Contact contact = contactService.find(contactId);
        String json = contactService.getJson(requestedFields, contact);

        return getOkResponse(json);
    }

    @DELETE
    @Path("{contactId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response removeContact(@PathParam("contactId") Long contactId) {

        logger.info("removing contact with id: " + contactId);

        contactService.remove(contactId);

        return getOkResponse();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response saveContact(String s) {

        logger.info("saveContact(" + s + ")");

        Contact contact = contactService.readValue(s, Contact.class);

        contactService.saveContact(contact);

        return Response.status(Response.Status.CREATED)
                       .build();
    }

    private Response getOkResponse() {
        return Response.ok().build();
    }

    private Response getOkResponse(String json) {
        return Response.ok(json).build();
    }
}
