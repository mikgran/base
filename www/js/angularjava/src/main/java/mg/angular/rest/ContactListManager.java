package mg.angular.rest;

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

import mg.angular.db.ContactListDao;
import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

@Path("/contactlist")
public class ContactListManager {

    private DBConfig dbConfig;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public ContactListManager() throws IOException {

        PropertyConfigurator.configure("log4j.properties");
        dbConfig = new DBConfig(new Config());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response listContacts() {

        logger.info("listing contacts");

        try {
            Connection connection = dbConfig.getConnection();

            ContactListDao contactListDao = new ContactListDao(connection);

            // TOIMPROVE: consider using the same class for both annotations
            // XmlRootElement + @Table(name = "contacts") ?
            List<mg.angular.db.Contact> dbContacts = contactListDao.findAll();

            System.out.println("Contacts::");
            dbContacts.stream()
                      .forEach(c -> System.out.println(c));

            if (hasContent(dbContacts)) {

            }

            List<mg.angular.rest.Contact> jsonContacts = getJSONContacts(dbContacts);

            jsonContacts.stream()
                        .forEach(c -> System.out.println(c));

            return Response.status(200)
                           .entity(jsonContacts.toString())
                           .build();

        } catch (DBValidityException | DBMappingException | ClassNotFoundException | SQLException e) {

            logger.error("Error while trying to fetch contacts from DB.", e);
        }

        // XXX replace with real fetch
        // List<Contact> contactList = new ArrayList<Contact>();
        // contactList.add(new Contact("name1", "e1@mail.com", "111"));
        // contactList.add(new Contact("name2", "e2@mail.com", "222"));

        // default: no content
        return Response.status(204)
                       .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setReservation(Contact contact) {

        logger.info(contact != null ? "inserting contact: " + contact.toString() : "got contact: null post request");

        return Response.status(200)
                       .entity("ok")
                       .build();
    }

    private List<Contact> getJSONContacts(List<mg.angular.db.Contact> dbContacts) {

        return dbContacts.stream()
                         .map(contact -> new mg.angular.rest.Contact(contact))
                         .collect(Collectors.toList());
    }

}
