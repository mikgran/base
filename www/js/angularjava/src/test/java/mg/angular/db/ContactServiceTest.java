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
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

public class ContactServiceTest {

    public static final String PHONE_123_4567 = "123 4567";
    public static final String TESTEY_TESTFUL_AT_MAIL_DOT_COM = "testey.testful@mail.com";
    public static final String TESTEY_TESTFUL = "Testey Testful";
    public static final String EMAIL = "test.name@email.com";
    public static final String NAME = "Test Name";
    public static final String PHONE = "(111) 111-1111";
    public static final Contact contact = new Contact(0L, NAME, EMAIL, PHONE);
    public static final Contact contact2 = new Contact(0L, NAME + "2", EMAIL + "2", PHONE + "2");

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
    public void findAllTest() throws DBValidityException, SQLException, ClassNotFoundException, DBMappingException {

        ContactService contactService = new ContactService(connection);

        List<Contact> contacts = contactService.findAll();

        assertNotNull("contacts should not be null", contacts);
        assertEquals("contacts size should be: ", 2, contacts.size());
    }

    @Test
    public void saveContact() throws ClassNotFoundException, SQLException, DBValidityException, DBMappingException {

        Contact contact3 = new Contact(0L, TESTEY_TESTFUL, TESTEY_TESTFUL_AT_MAIL_DOT_COM, PHONE_123_4567);

        ContactService contactService = new ContactService(connection);
        contactService.saveContact(contact3);

        DB db = new DB(connection);
        Contact contact4 = (Contact) new Contact().field("id").is(3L);
        Contact candidateContact = db.findBy(contact4);

        Contact expectedContact = new Contact(3L, TESTEY_TESTFUL, TESTEY_TESTFUL_AT_MAIL_DOT_COM, PHONE_123_4567);
        assertContactsEqual(expectedContact, candidateContact);
    }

    private void assertContactsEqual(Contact expectedContact, Contact candidateContact) {

        // TOCONSIDER: create an equals and hash methods for comparisons and use that?
        assertNotNull(candidateContact);
        assertEquals("id should be: ", expectedContact.getId(), candidateContact.getId());
        assertEquals("name should be: ", expectedContact.getName(), candidateContact.getName());
        assertEquals("email should be: ", expectedContact.getEmail(), candidateContact.getEmail());
        assertEquals("phone should be: ", expectedContact.getPhone(), candidateContact.getPhone());
    }

}