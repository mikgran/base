package mg.angular.rest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
public class ContactlistManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private DBConfig dbConfig;

    public ContactlistManager() throws IOException {

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

            // TOIMPROVE: use the same class for both annotations XmlRootElement + @Table(name = "contacts") ?
            List<mg.angular.db.Contact> dbContacts = contactListDao.findAll();

            getJSONContacts(dbContacts);

        } catch (DBValidityException | DBMappingException | ClassNotFoundException | SQLException e) {

            logger.error("Error while trying to fetch contacts from DB.", e);

        }

        List<Contact> contactList = new ArrayList<Contact>();

        contactList.add(new Contact("name1", "e1@mail.com", "111"));
        contactList.add(new Contact("name2", "e2@mail.com", "222"));

        return Response.status(200)
                       .entity(contactList.toString())
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

    private void getJSONContacts(List<mg.angular.db.Contact> dbContacts) {



    }

}
