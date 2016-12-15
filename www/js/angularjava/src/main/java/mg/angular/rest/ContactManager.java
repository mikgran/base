package mg.angular.rest;

import static java.lang.String.format;
import static mg.util.Common.hasContent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.angular.db.ContactService;
import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

@Path("/contacts")
public class ContactManager {

    private static final int CREATED = 201;
    private static final int INTERNAL_ERROR = 503;
    private static final int NO_CONTENT = 204;
    private DBConfig dbConfig;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public ContactManager() throws IOException {

        PropertyConfigurator.configure("log4j.properties");
        dbConfig = new DBConfig(new Config());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllContacts() {

        logger.info("listing contacts");

        try {
            Connection connection = dbConfig.getConnection();
            ContactService contactService = new ContactService(connection);
            List<mg.angular.db.Contact> dbContacts = contactService.findAll();

            List<Contact> restContacts;
            restContacts = dbContacts.stream()
                                     .map(dbContact -> new mg.angular.rest.Contact(dbContact))
                                     .collect(Collectors.toList());

            if (hasContent(restContacts)) {

                logger.info(restContacts.toString());

                return Response.ok()
                               .entity(restContacts.toString())
                               .build();
            } else {
                return Response.status(NO_CONTENT)
                               .build();
            }

        } catch (DBValidityException | DBMappingException | ClassNotFoundException | SQLException e) {

            logger.error("Error while trying to fetch contacts from DB.", e);

            // TOCONSIDER: exit program on major failure / reporting / monitoring
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
            Contact restContact = objectMapper.readValue(s, mg.angular.rest.Contact.class);

            try {
                ContactService contactService = new ContactService(dbConfig.getConnection());

                contactService.saveContact(new mg.angular.db.Contact(0L,
                                                                     restContact.getName(),
                                                                     restContact.getEmail(),
                                                                     restContact.getPhone()));

            } catch (ClassNotFoundException | SQLException | DBValidityException e) {

                logger.error("Error while trying to save a contact to DB.", e);

                // TOCONSIDER: exit program on major failure / reporting / monitoring
                return Response.status(INTERNAL_ERROR)
                               .build();
            }

            System.out.println("the rest contact:: '" + restContact + "'");

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
