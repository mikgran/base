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

@Path("/contacts")
public class ContactResource {

    // TOCONSIDER: exit program on major failure and-or reporting and-or monitoring
    // TOIMPROVE: give a proper REST API error message in case of a failure.
    // TOIMPROVE: sorting, listing

    // XXX: sorting
    // XXX: REST: remove/delete

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts(@QueryParam("fields") String requestedFields,
        @QueryParam("") String sorts) {

        logger.info("getting all contacts");
        Response response = null;

        try {
            ContactService contactService = new ContactService();
            List<Contact> contacts = contactService.findAll(); // TOCONSIDER: change DB query to fetch only requested fields.

            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(getContactFilters(requestedFields));
            String contactJson = writer.writeValueAsString(contacts);

            if (!contactJson.matches(".*[a-zA-Z]+.*")) { // case [{},{}], user provided funky query. TOCONSIDER: return an InvalidRequest
                contactJson = "{}";
                response = Response.status(Response.Status.NO_CONTENT)
                                   .entity(contactJson)
                                   .build();

            } else if (hasContent(contacts)) {
                response = Response.status(Response.Status.OK)
                                   .entity(contactJson)
                                   .build();

            } else {
                contactJson = "{}";
                response = Response.status(Response.Status.NO_CONTENT)
                                   .entity(contactJson)
                                   .build();
            }
        } catch (SQLException | DBValidityException | DBMappingException | ClassNotFoundException e) {

            logger.error("Error while trying to findAll contacts: ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .build();
        } catch (JsonProcessingException e) {

            logger.error("Error while trying to findAll contacts: ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .build();
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

            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .build();

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
            String[] excludeFields = {"constraints", "fetched", "fieldName"};
            contactFilters = SimpleBeanPropertyFilter.serializeAllExcept(excludeFields);
        }

        return new SimpleFilterProvider().addFilter("contactFilter", contactFilters);
    }

}
