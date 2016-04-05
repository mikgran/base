package mg.angular.db;

import static mg.util.Common.close;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.db.persist.DBValidityException;

public class ContactListDaoTest {

    public static final String EMAIL = "test.name@email.com";
    public static final String NAME = "Test Name";
    public static final String PHONE = "(111) 111-1111";
    public static final Contact contact = new Contact(0, NAME, EMAIL, PHONE);
    public static final Contact contact2 = new Contact(0, NAME + "2", EMAIL + "2", PHONE + "2");

    private static Connection connection;

    // TODO: angular: add tests for db access
    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("angularjavatest");

        DB db = new DB(connection);
        db.dropTable(contact);
        db.createTable(contact);
        db.save(contact);
        db.save(contact2);
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        close(connection);
    }

    @Test
    public void findAllTest() throws DBValidityException, SQLException {

        ContactListDao contactListDao = new ContactListDao();

        List<Contact> contacts = contactListDao.findAll();

        assertNotNull("contacts should not be null", contacts);
        assertEquals("contacts size should be: ", 2, contacts.size());
    }

}
