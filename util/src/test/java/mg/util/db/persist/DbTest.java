package mg.util.db.persist;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.support.Contact;
import mg.util.db.persist.support.Contact2;
import mg.util.db.persist.support.Location;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Person2;
import mg.util.db.persist.support.Todo;

public class DBTest {

    private static Connection connection;

    @BeforeClass
    public static void setupOnce() throws IOException {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        Common.close(connection);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // drops, creates, inserts data and saves all in one method: worst ever
    // shit, but tests are run independently - therefore - this is the only
    // way.
    @Test
    public void tableTest() throws Exception {

        String name = "name";
        String email = "name@email.com";
        String phone = "(111) 111-1111";

        String name2 = "name2";
        String email2 = "name2@email.com";
        String phone2 = "(222) 222-2222";

        String newName = "newName";

        Contact contact = new Contact(0, name, email, phone);
        Contact contact2 = new Contact(0, name2, email2, phone2);
        DB db = new DB(connection);

        // try to simulate a potential work flow:
        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact);

            ResultSet resultSet = statement.executeQuery(format("SHOW TABLES LIKE '%s'", "contacts"));
            if (resultSet.next()) {
                fail(format("database should not contain a %s table.", "contacts"));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.createTable(contact);

            ResultSet resultSet = statement.executeQuery(format("SHOW TABLES LIKE '%s'", "contacts"));
            if (!resultSet.next()) {
                fail(format("database should contain a %s table.", "contacts"));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.save(contact);

            ResultSet resultSet = statement.executeQuery(format("SELECT * FROM %s;", "contacts"));

            if (!resultSet.next()) {
                fail("database should contain at least 1 row of contacts.");
            }

            assertEquals("after save() contact should have id.", 1, contact.getId());
            assertEquals(name, resultSet.getString("name"));
            assertEquals(email, resultSet.getString("email"));
            assertEquals(phone, resultSet.getString("phone"));

            db.save(contact2);

            ResultSet resultSet2 = statement.executeQuery(format("SELECT * FROM contacts WHERE id = %d;", contact2.getId()));

            if (!resultSet2.next()) {
                fail("database should contain a contact with id 2");
            }

            assertEquals(format("after save() %s should have id", "contacts"), 2, contact2.getId());
            assertEquals(name2, resultSet2.getString("name"));
            assertEquals(email2, resultSet2.getString("email"));
            assertEquals(phone2, resultSet2.getString("phone"));

            contact.setName(newName);

            db.save(contact);

            ResultSet resultSet3 = statement.executeQuery(format("SELECT * FROM contacts WHERE id = %d;", contact.getId()));

            if (!resultSet3.next()) {
                fail("selecting all for id 1 should return contact.");
            }

            assertEquals("after name change to the newName should be:", newName, resultSet3.getString("name"));
            assertEquals(email, resultSet3.getString("email"));
            assertEquals(phone, resultSet3.getString("phone"));
        }
    }

    @Test
    public void tableTest2() throws Exception {

        String name = "name";
        String email = "name@email.com";
        String phone = "(111) 111-1111";

        String name2 = name + "2";
        String email2 = email + "2";
        String phone2 = "(222) 222-2222";

        String name3 = name + "3";
        String email3 = email + "3";
        String phone3 = "(333) 333-3333";

        Contact2 contact1 = new Contact2(0, name, email, phone);
        Contact2 contact2 = new Contact2(0, name2, email2, phone2);
        Contact2 contact3 = new Contact2(0, name3, email3, phone3);
        DB db = new DB(connection);

        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact1);
            db.createTable(contact1);
            db.save(contact1);

            ResultSet resultSet = statement.executeQuery(format(format("SELECT * FROM %s;", "contacts2")));
            if (!resultSet.next()) {
                fail("database should contain a row for contact 1.");
            }

            db.save(contact2);

            ResultSet resultSet2 = statement.executeQuery(format("SELECT * FROM contacts2 WHERE id = %d;", contact2.getId()));
            if (!resultSet2.next()) {
                fail("database should contain a row for contact 2.");
            }

            db.remove(contact1);

            ResultSet resultSet3 = statement.executeQuery(format("SELECT * FROM contacts2 WHERE id = %d;", contact1.getId()));
            if (resultSet3.next()) {
                fail("database should not contain a row for contact 1.");
            }

            db.save(contact3);

            ResultSet resultSet4 = statement.executeQuery(format("SELECT * FROM contacts2 WHERE id = %d;", contact3.getId()));
            if (!resultSet4.next()) {
                fail("database should contain a row for contact 3");
            }

            assertEquals(format("after save() %s should have id", "contacts2"), 3, contact3.getId());
            assertEquals(name3, resultSet4.getString("name"));
            assertEquals(email3, resultSet4.getString("email"));
            assertEquals(phone3, resultSet4.getString("phone"));

            db.remove(contact2);
            db.remove(contact3);

            ResultSet resultSet5 = statement.executeQuery(format(format("SELECT * FROM %s;", "contacts2")));
            if (resultSet5.next()) {
                fail("database should not contain any rows after removing all three test contacts.");
            }
        }
    }

    @Test
    public void tableTest3() throws Exception {

        DB db = new DB(connection);

        Location location = new Location("place1");
        Location location2 = new Location("place2");
        Todo todo = new Todo("something todo", Collections.emptyList());
        Todo todo2 = new Todo("else todo", Arrays.asList(location, location2));
        Person person = new Person("first1", "last1", Arrays.asList(todo, todo2));

        db.dropTable(person);
        db.dropTable(todo);
        db.dropTable(location);
        db.createTable(person);
        db.createTable(todo);
        db.createTable(location);

        // composition: perform a cascade update on all collections that are
        // tagged with collection annotations
        db.save(person);

        try (Statement statement = connection.createStatement()) {

            assertThatAtLeastOneRowExists(statement, format("SELECT * FROM %s;", "persons"), "persons");
            assertThatAtLeastOneRowExists(statement, format("SELECT * FROM %s;", "todos"), "todos");
        }

        // TOIMPROVE: add test coverage; test for inserted composition contents - even though implicitly
        // tested by other tests.
    }

    @Test
    public void testFindById() throws SQLException, DBValidityException, ResultSetMapperException {

        DB db = new DB(connection);

        Person2 person1 = new Person2("first111", "last222", Collections.emptyList());

        db.dropTable(person1);
        db.createTable(person1);
        db.save(person1);

        Person2 person2 = new Person2();
        person2.setId(1);
        Person2 fetchedPerson1 = db.findById(person2);

        assertNotNull(fetchedPerson1);
        assertEquals("id should be: ", 1, fetchedPerson1.getId());
        assertEquals("first name should be: ", "first111", fetchedPerson1.getFirstName());
        assertEquals("last name should be: ", "last222", fetchedPerson1.getLastName());
        assertEquals("fetched person should have an empty todos list: ", Collections.emptyList(), fetchedPerson1.getTodos());

        Person2 person3 = new Person2(); // no id provided -> should result in empty object
        Person2 fetchedPerson2 = db.findById(person3);

        assertNotNull(fetchedPerson2);
        assertEquals("id should be: ", 0, fetchedPerson2.getId());
        assertEquals("first name should be: ", "", fetchedPerson2.getFirstName());
        assertEquals("last name should be: ", "", fetchedPerson2.getLastName());
        assertEquals("fetched person should have an empty todos list: ", Collections.emptyList(), fetchedPerson2.getTodos());
    }

    private void assertThatAtLeastOneRowExists(Statement statement, String query, String tableName) throws SQLException {
        logger.debug("SQL:: " + query);
        ResultSet resultSet = statement.executeQuery(query);
        if (!resultSet.next()) {
            fail("database should contain a row for " + tableName);
        }
    }

}
