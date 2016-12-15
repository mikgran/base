package mg.angular.db;

import static mg.util.validation.Validator.validateNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import mg.util.db.persist.DB;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

public class ContactService {

    private Connection connection;

    public ContactService(Connection connection) throws ClassNotFoundException, SQLException {

        this.connection = validateNotNull("connection", connection);
    }

    public List<Contact> findAll() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        Contact contact = new Contact();

        List<Contact> allContacts = db.findAllBy(contact);

        return allContacts;
    }

    public void saveContact(Contact contact) throws SQLException, DBValidityException {

        DB db = new DB(connection);

        db.save(contact);
    }


}