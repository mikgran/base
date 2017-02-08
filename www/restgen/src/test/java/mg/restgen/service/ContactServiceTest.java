package mg.restgen.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import mg.restgen.db.Contact;
import mg.restgen.rest.ContactService;
import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.db.persist.DBMappingException;
import mg.util.db.persist.DBValidityException;

public class ContactServiceTest {

    public static final String EMAIL = "__test.name@email.com";
    public static final String NAME = "__Test Name";
    public static final String PHONE = "__(111) 111-1111";
    public static final String PHONE_123_4567 = "__123 4567";
    public static final String TESTEY_TESTFUL = "__Testey Testful";
    public static final String TESTEY_TESTFUL_AT_MAIL_DOT_COM = "__testey.testful@mail.com";
    public static final Contact contact = new Contact(0L, NAME, EMAIL, PHONE);
    public static final Contact contact2 = new Contact(0L, NAME + "2", EMAIL + "2", PHONE + "2");

    private static Connection connection;
    private static ContactService contactService;

    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("restgentest");

        DB db = new DB(connection);
        db.dropTable(contact);
        db.createTable(contact);
        db.save(contact);
        db.save(contact2);

        contactService = new ContactService();
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        Common.close(connection);
    }

    @Test
    public void testFindAllTest() throws DBMappingException, DBValidityException, SQLException, ClassNotFoundException {

        List<Contact> contacts = contactService.findAll();

        assertNotNull("contacts should not be null", contacts);
        assertEquals("contacts size should be: ", 2, contacts.size());
        assertContactsEqual(contact, contacts.get(0));
        assertContactsEqual(contact2, contacts.get(1));
    }

    // TOIMPROVE: coverage
    @Test
    public void testFindAllTestMVMap() {

        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

        queryParameters.putSingle("searchTerm", "name");
        queryParameters.putSingle("q", NAME);

        List<Contact> candidateContacts = contactService.findAll(queryParameters);

        assertNotNull(candidateContacts);
        assertEquals("there should be contact: ", 1, candidateContacts.size());
        assertContactsEqual(contact, candidateContacts.get(0));
    }

    @Test
    public void testFindOne() throws DBMappingException, DBValidityException, SQLException, ClassNotFoundException {

        Contact candidateContact = contactService.find(2L);

        assertNotNull(candidateContact);
        assertContactsEqual(contact2, candidateContact);
    }

    @Test
    public void testRemoveContact() throws SQLException, DBValidityException, DBMappingException, IllegalArgumentException, ClassNotFoundException {

        Contact contact3 = new Contact(0L, TESTEY_TESTFUL, TESTEY_TESTFUL_AT_MAIL_DOT_COM, PHONE_123_4567);
        Contact candidateContact;

        contact3.setConnectionAndDB(connection);
        contact3.save();

        candidateContact = contact3.findById();
        assertNotNull(candidateContact);
        assertContactsEqual(contact3, candidateContact);

        contactService.remove(contact3.getId());

        candidateContact = contact3.findById();
        assertNull(candidateContact);
    }

    @Test
    public void testSaveContact() throws DBValidityException, DBMappingException, SQLException, ClassNotFoundException {

        Contact contact3 = new Contact(0L, TESTEY_TESTFUL, TESTEY_TESTFUL_AT_MAIL_DOT_COM, PHONE_123_4567);

        contactService.saveContact(contact3);

        DB db = new DB(connection);
        Contact contact4 = (Contact) new Contact().field("name").is(TESTEY_TESTFUL);
        Contact candidateContact = db.findBy(contact4);

        Contact expectedContact = new Contact(3L, TESTEY_TESTFUL, TESTEY_TESTFUL_AT_MAIL_DOT_COM, PHONE_123_4567);
        assertContactsEqual(expectedContact, candidateContact);
    }

    private void assertContactsEqual(Contact expectedContact, Contact candidateContact) {

        // TOCONSIDER: create an equals and hash methods for comparisons and use that?
        assertNotNull(candidateContact);
        assertEquals("name should be: ", expectedContact.getName(), candidateContact.getName());
        assertEquals("email should be: ", expectedContact.getEmail(), candidateContact.getEmail());
        assertEquals("phone should be: ", expectedContact.getPhone(), candidateContact.getPhone());
    }

}
