package mg.restgen.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.restgen.db.Contact;
import mg.restgen.service.ContactService;
import mg.restgen.service.CrudService;
import mg.restgen.service.RestGen;
import mg.restgen.service.ServiceException;
import mg.restgen.service.ServiceResult;
import mg.util.Common;
import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.functional.option.Opt;

@Path("/restgen")
public class RestGenResource {

    // TODO: Generic service class map (Clazz.class -> MyService.class)
    //     path: api3/{clazzName}/{id} -> @Path("{clazzName}") + @PathParam("clazzName") String clazzName
    //     serviceMap.get(clazzName).<operationNamePlusParameters> OR inject based on the Clazz.class

    // XXX: change all methods to use RestService generic <class> CRUD service.

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private ContactService contactService = new ContactService();

    public RestGenResource() throws IllegalArgumentException, ClassNotFoundException, SQLException, IOException {
        CrudService crudService = new CrudService(new DBConfig(new Config())); // TOIMPROVE: remove this and replace with something that doesn't dangle connections for lenghty period of time
        RestGen.register(Contact.class, crudService, "put");
        RestGen.register(Contact.class, crudService, "get"); // TOIMPROVE: add register(Class<?>, RestService, String command...) to avoid repeating commands.
    }

    @GET
    @Path("id/{className}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAll(@PathParam("className") String className,
        @Context UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        logger.info("getAllContacts(queryParameters: " + queryParameters + ")");

        // TODO: move this to RestGen

        // FIXME: call RestGen



        // - all
        // - by id
        // - by search

        List<String> requestedFieldsList = queryParameters.get("fields");
        String requestedFields = Common.splitToStream(requestedFieldsList, ",")
                                       .collect(Collectors.joining(","));

        List<Contact> contacts = contactService.findAll(queryParameters);
        String json = contactService.getJson(requestedFields, contacts);

        // return null;
        return getOkResponse(json);
    }

    @GET
    @Path("id/{className}/{contactId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getContact(@PathParam("className") String className,
        @PathParam("contactId") Long contactId,
        @DefaultValue("") @QueryParam("fields") String requestedFields) {

        logger.info("getContact(" + contactId + ")");

        Contact contact = contactService.find(contactId);
        String json = contactService.getJson(requestedFields, contact);

        return getOkResponse(json);
    }

    @DELETE
    @Path("id/{className}/{contactId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response removeContact(@PathParam("className") String className,
        @PathParam("contactId") Long contactId) {

        logger.info("removing contact with id: " + contactId);

        contactService.remove(contactId);

        return getOkResponse();
    }

    @POST
    @Path("id/{className}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response saveContact(@PathParam("className") String className, String json) {

        logger.info("saveContact(" + json + ")");

        Opt<Response> returnValue = Opt.empty();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("nameRef", className);
            parameters.put("command", "put");
            List<ServiceResult> serviceResults = RestGen.service(json, parameters);

            // construct the returnValue from all payloads
            serviceResults.stream()
                          .map(t -> t);

        } catch (ServiceException e) {

//            Opt.of(e.serviceResult)
//               .map(t -> t)
//
//               ;

            logger.error(e.getMessage());
            returnValue = Opt.of(getResponseForInternalError());
        }

//        Contact contact = contactService.readValue(json, Contact.class);
//
//        contactService.saveContact(contact);

        return Response.status(Response.Status.CREATED)
                       .build();
    }

    private Response getOkResponse() {
        return Response.ok().build();
    }

    private Response getOkResponse(String json) {
        return Response.ok(json).build();
    }

    private Response getResponseForInternalError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
