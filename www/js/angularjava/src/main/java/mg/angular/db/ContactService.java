package mg.angular.db;

import java.io.IOException;
import java.sql.SQLException;
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

    public ContactService() {

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

    public Contact find(long id) throws IllegalArgumentException, ClassNotFoundException, SQLException, DBValidityException, DBMappingException {

        Contact contact = null;
        DB db = new DB(dbConfig.getConnection());
        contact = db.findById(new Contact().setId(id));

        return contact;
    }

    public List<Contact> findAll() throws ClassNotFoundException, SQLException, DBValidityException, DBMappingException {

        Contact contact = new Contact(dbConfig.getConnection());

        List<Contact> allContacts = contact.findAll();

        return allContacts;
    }

    public Contact saveContact(Contact contact) throws SQLException, DBValidityException, ClassNotFoundException {

        contact.setConnectionAndDB(dbConfig.getConnection());

        contact.save();

        return contact;
    }

}
