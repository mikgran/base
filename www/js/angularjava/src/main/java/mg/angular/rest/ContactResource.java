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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    // TOIMPROVE: sorting, listing

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts() {

        logger.info("getting all contacts");

            ContactService contactService = new ContactService();
            List<Contact> contacts = contactService.findAll();

            if (hasContent(contacts)) {

                logger.info(contacts.toString());

                return Response.ok()
                               .entity(contacts.toString())
                               .build();
            } else {

                return Response.status(Response.Status.NO_CONTENT)
                               .build();
            }

            // logger.error("Error while trying to fetch contacts from DB.", e);

//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                           .build();
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
    public Response saveNewContact(String s) {

        logger.info(format("got post: %s", s));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Contact contact = objectMapper.readValue(s, Contact.class);

            ContactService contactService = new ContactService();
            contactService.saveContact(contact);

            return Response.status(Response.Status.CREATED)
                           .entity("ok")
                           .build();

        } catch (ClassNotFoundException | SQLException | DBValidityException e) {

            logger.error("Error while trying to save a contact to DB.", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .build();

        } catch (IOException e) {

            logger.error("Unable to parse incoming json string.", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .build();
        }

    }

    // XXX: REST: remove/delete next
}
