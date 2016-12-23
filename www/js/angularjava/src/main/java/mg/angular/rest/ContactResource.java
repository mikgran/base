package mg.angular.rest;

import static java.lang.String.format;
import static mg.util.Common.hasContent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.angular.db.Contact;
import mg.angular.db.ContactService;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;
import mg.util.functional.function.ThrowingFunction;

@Path("/contacts")
public class ContactResource {

    // TOCONSIDER: exit program on major failure and-or reporting and-or monitoring
    // TOIMPROVE: give a proper REST API error message in case of a failure.
    // TOIMPROVE: sorting, listing

    // XXX: listing only based on field names -> reflective ObjectMapper (naive version of..)
    // XXX: sorting

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts() {

        logger.info("getting all contacts");
        Response response = null;

        try {

            ObjectMapper mapper = new ObjectMapper();
            // first, construct filter provider to exclude all properties but 'name', bind it as 'myFilter'
            FilterProvider filters = new SimpleFilterProvider().addFilter("contactFilter", SimpleBeanPropertyFilter.serializeAllExcept("name"));

            ObjectWriter writer = mapper.writer(filters);
            // and then serialize using that filter provider:
            String json = writer.writeValueAsString(new Contact(1L, "a b", "a.b@mail.com", "123"));

            System.out.println("JSON:: " + json);

            ContactService contactService = new ContactService();
            List<Contact> contacts = contactService.findAll();

            String collect = contacts.stream()
            .map((ThrowingFunction<Contact, String, Exception>) contact -> {
                return writer.writeValueAsString(contact);
            }).collect(Collectors.joining(", "));

            System.out.println("collect:: " + collect);

            if (hasContent(contacts)) {
                response = Response.status(Response.Status.OK)
                                   .entity(contacts.toString())
                                   .build();
            } else {
                response = Response.status(Response.Status.NO_CONTENT)
                                   .build();
            }
        } catch (SQLException | DBValidityException | DBMappingException | ClassNotFoundException e) {

            logger.error("Error while trying to findAll contacts: ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .build();
        } catch (JsonProcessingException e) {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .build();
            System.out.println("ERR:: " + e);
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

    // XXX: REST: remove/delete next
}
