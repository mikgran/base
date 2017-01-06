package mg.angular.db;

import static mg.util.rest.QuerySortParameterType.SORT_ASCENDING;

import java.io.IOException;
import java.sql.Connection;
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
import mg.util.rest.QuerySortParameter;

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

    public Contact find(long id) throws IllegalArgumentException, ClassNotFoundException, SQLException, DBValidityException, DBMappingException {

        try(Connection connection = dbConfig.getConnection()) {

            Contact contact = null;
            DB db = new DB(connection);
            contact = db.findById(new Contact().setId(id));
            return contact;
        }
    }

    public List<Contact> findAll() throws ClassNotFoundException, SQLException, DBValidityException, DBMappingException {

        try (Connection connection = dbConfig.getConnection()) {

            Contact contact = new Contact(connection);
            List<Contact> allContacts = contact.findAll();
            return allContacts;
        }
    }

    public List<Contact> findAll(List<QuerySortParameter> querySortParameters) throws ClassNotFoundException, SQLException, DBValidityException, DBMappingException {

        try (Connection connection = dbConfig.getConnection()) {

            Contact contact = new Contact(connection);

            // TOCONSIDER, TOIMPROVE: generalize to funtions QuerySortParameter -> contact (with DSL for searching)
            // contact.field("fieldName").orderByAscending() // intermediate + terminal operation.
            querySortParameters.stream()
                               .forEach(sortParameter -> {
                                   contact.field(sortParameter.getParameter());
                                   if (sortParameter.getType() == SORT_ASCENDING) {
                                       contact.orderByAscending();
                                   } else {
                                       contact.orderByDescending();
                                   }
                               });

            List<Contact> allContacts = contact.findAll();

            return allContacts;
        }
    }

    // TOCONSIDER: return removed contact/affected object count from the database?
    public void remove(Long id) throws SQLException, DBValidityException, IllegalArgumentException, ClassNotFoundException {

        Contact contact = new Contact();
        contact.setConnectionAndDB(dbConfig.getConnection());
        contact.setId(id);
        contact.remove();
    }

    public Contact saveContact(Contact contact) throws SQLException, DBValidityException, ClassNotFoundException {

        contact.setConnectionAndDB(dbConfig.getConnection());
        contact.save();
        return contact;
    }

}
