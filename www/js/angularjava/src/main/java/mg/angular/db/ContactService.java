package mg.angular.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

import mg.util.Config;
import mg.util.db.DBConfig;
import mg.util.db.persist.DB;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

public class ContactService {

    private DBConfig dbConfig;

    public ContactService() throws ClassNotFoundException, SQLException, IOException {

        PropertyConfigurator.configure("log4j.properties");
        dbConfig = new DBConfig(new Config());
    }

    // XXX: create a class that accepts parameter formatted sorting instructions and validates against T extends Persistable fields.

    public Contact find(long id) throws ClassNotFoundException, SQLException, DBValidityException, DBMappingException {

        DB db = new DB(dbConfig.getConnection());
        Contact contact = db.findById(new Contact().setId(id));
        return contact;
    }

    public List<Contact> findAll() throws SQLException, DBValidityException, DBMappingException, ClassNotFoundException {

        Connection connection = dbConfig.getConnection();
        DB db = new DB(connection);

        Contact contact = new Contact();
        List<Contact> allContacts = db.findAllBy(contact);
        return allContacts;
    }

    public void saveContact(Contact contact) throws SQLException, DBValidityException, ClassNotFoundException {

        Connection connection = dbConfig.getConnection();
        DB db = new DB(connection);

        db.save(contact);
    }


}
