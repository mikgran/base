package mg.angular.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.db.persist.DB;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

public class ContactService {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private DBConfig dbConfig;

    public ContactService()  {

        try {
            PropertyConfigurator.configure("log4j.properties");
            dbConfig = new DBConfig(new Config());

        } catch (IOException e) {

            // client can not recover from this exception, and the server should shut down:
            String msg = "Unable to initialize ContactService: ";
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    // XXX: create a class that accepts parameter formatted sorting instructions and validates against T extends Persistable fields.

    public Contact find(long id) {

        Contact contact = null;
        try {
            DB db = new DB(dbConfig.getConnection());
            contact = db.findById(new Contact().setId(id));

        } catch (IllegalArgumentException | ClassNotFoundException | SQLException | DBValidityException | DBMappingException e) {

            logger.error("Unable to find contact for id: " + id, e);
            contact =
        }
        return contact;
    }

    public List<Contact> findAll() {

        List<Contact> allContacts;

        try {

            Connection connection = dbConfig.getConnection();
            DB db = new DB(connection);

            Contact contact = new Contact();
            allContacts = db.findAllBy(contact);

        } catch (SQLException | DBValidityException | DBMappingException | ClassNotFoundException e) {

            logger.error("Error while trying to findAll contacts: ", e);

            allContacts = Collections.emptyList();
        }

        return allContacts;
    }

    public void saveContact(Contact contact) throws SQLException, DBValidityException, ClassNotFoundException {

        Connection connection = dbConfig.getConnection();
        DB db = new DB(connection);

        db.save(contact);
    }

}
