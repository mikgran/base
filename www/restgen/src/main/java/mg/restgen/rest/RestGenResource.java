package mg.restgen.rest;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.functional.option.Opt;

@Path("/restgen")
public class RestGenResource {

    // TODO: Generic service class map (Clazz.class -> MyService.class)
    //     path: api3/{clazzName}/{id} -> @Path("{clazzName}") + @PathParam("clazzName") String clazzName
    //     serviceMap.get(clazzName).<operationNamePlusParameters> OR inject based on the Clazz.class

    private static final String GET = "get";
    private static final String PUT = "put";
    // XXX: change all methods to use RestService generic <class> CRUD service.
    private ServiceResult srRef = new ServiceResult(0, "");
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private ContactService contactService = new ContactService();
    private RestGen restGen;

    public RestGenResource() throws IllegalArgumentException, ClassNotFoundException, SQLException, IOException {
        CrudService crudService = new CrudService(new DBConfig(new Config())); // TOIMPROVE: remove this and replace with something that doesn't dangle connections for lenghty period of time

        restGen = RestGen.init();
        restGen.register(Contact.class, crudService, PUT);
        restGen.register(Contact.class, crudService, GET); // TOIMPROVE: add register(Class<?>, RestService, String command...) to avoid repeating commands.
    }

    @GET
    @Path("id/{className}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllIds(@PathParam("className") String className,
        @Context UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        logger.info("getAllIds(queryParameters: " + queryParameters + ")");

        Opt<Response> response = service(uriInfo, className, "", GET);

        return response.getOrElseGet(() -> getResponseForInternalError());
    }

    @GET
    @Path("id/{className}/{genId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getId(@PathParam("className") String className,
        @PathParam("genId") Long genId,
        @DefaultValue("") @QueryParam("fields") String requestedFields) {

        logger.info("getId(" + genId + ")");

        Contact contact = contactService.find(genId);
        String json = contactService.getJson(requestedFields, contact);

        return getOkResponse(json);
    }

    @DELETE
    @Path("id/{className}/{genId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response removeId(@PathParam("className") String className,
        @PathParam("genId") Long genId) {

        logger.info("removeId(" + className + "  with id: " + genId + ")");

        contactService.remove(genId);

        return getOkResponse();
    }

    @POST
    @Path("id/{className}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response saveId(@Context UriInfo uriInfo, @PathParam("className") String className, String json) {

        logger.info("saveId(className: " + className + ", json: " + json + ")");

        Opt<Response> response = service(uriInfo, className, json, PUT);

        return response.getOrElseGet(() -> getResponseForInternalError());
    }

    private Opt<String> getMessageFromFirstResult(List<ServiceResult> serviceResults) {
        // returning the put -> 201 / error for return signal only, use custom for multiple return signals
        // the first of the results should be the put, rest of the results are the side effects.
        // construct the Response on the basis of the main action
        // TOIMPROVE: construct response teling about failing side effects.
        Opt<ServiceResult> sr = serviceResults.stream()
                                              .map(Opt::of)
                                              .findFirst() // assuming the 1st serviceResult is the main query, and the rest are side-effects from utility services.
                                              .get();

        return sr.match(srRef, i -> i.statusCode == 201, i -> i.message)
                 .match(srRef, i -> i.statusCode != 201 && i.statusCode > 0, i -> "")
                 .right()
                 .map(obj -> obj.toString());
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

    private Map<String, Object> getServiceParameters(String className, String command) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nameref", className);
        parameters.put("command", command);
        return parameters;
    }

    private Opt<Response> service(UriInfo uriInfo, String className, String json, String command) {

        Opt<Response> returnValue;

        try {
            Map<String, Object> serviceParameters = getServiceParameters(className, command);

            List<ServiceResult> serviceResults = restGen.service(json, serviceParameters);

            Opt<String> msg = getMessageFromFirstResult(serviceResults);

            returnValue = msg.map(s -> URI.create(uriInfo.getPath() + "/" + s))
                             .map(uri -> Response.created(uri).build());

        } catch (ServiceException e) {

            logger.error("Exception while performing RestGen.service()", e);
            returnValue = Opt.of(getResponseForInternalError());
        }

        return returnValue;
    }
}
