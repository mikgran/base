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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.angular.db.Contact;
import mg.angular.db.ContactService;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

@Path("/contacts")
public class ContactManager {

    private static final int CREATED = 201; // TOCONSIDER: create a common collection class for these.
    private static final int INTERNAL_ERROR = 503;
    private static final int NO_CONTENT = 204;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public ContactManager() throws IOException {

    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts() {

        logger.info("listing contacts");

        try {
            ContactService contactService = new ContactService();
            List<Contact> contacts = contactService.findAll();

            if (hasContent(contacts)) {

                logger.info(contacts.toString());

                return Response.ok()
                               .entity(contacts.toString())
                               .build();
            } else {

                return Response.status(NO_CONTENT)
                               .build();
            }

        } catch (DBValidityException | DBMappingException | ClassNotFoundException | SQLException | IOException e) {

            logger.error("Error while trying to fetch contacts from DB.", e);

            // TOCONSIDER: exit program on major failure and-or reporting and-or monitoring
            // TOIMPROVE: give a proper REST API error message in case of a failure.
            return Response.status(INTERNAL_ERROR)
                           .build();
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response saveNewContact(String s) {

        logger.info(format("got post: %s", s));

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Contact contact = objectMapper.readValue(s, Contact.class);

            try {
                ContactService contactService = new ContactService();
                contactService.saveContact(contact);

            } catch (ClassNotFoundException | SQLException | DBValidityException e) {

                logger.error("Error while trying to save a contact to DB.", e);

                // TOCONSIDER: exit program on major failure / reporting / monitoring
                // TOIMPROVE: give a proper REST API error message in case of a failure.
                return Response.status(INTERNAL_ERROR)
                               .build();
            }

            return Response.status(CREATED)
                           .entity("ok")
                           .build();

        } catch (IOException e) {

            logger.error("Unable to parse incoming json string.", e);

            // TOCONSIDER: exit program on major failure / reporting / monitoring
            return Response.status(INTERNAL_ERROR)
                           .build();
        }

    }

    // XXX: REST: remove/delete next
}
