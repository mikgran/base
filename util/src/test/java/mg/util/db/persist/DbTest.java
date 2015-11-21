package mg.util.db.persist;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;

public class DbTest {

    private static final String TEST_DB_NAME = "dbotest";
    private static final String TEST_DB_TABLE_NAME = "contacts";
    private static final String TEST_DB_TABLE_NAME2 = "contacts2";
    private static final String SELECT_FROM_CONTACTS_WHERE_ID_IS = "SELECT * FROM contacts WHERE id = %d;";
    private static final String SELECT_FROM_CONTACTS2_WHERE_ID_IS = "SELECT * FROM contacts2 WHERE id = %d;";
    private static final String SELECT_ALL_FROM_CONTACTS = format("SELECT * FROM %s;", TEST_DB_TABLE_NAME);
    private static final String SELECT_ALL_FROM_CONTACTS2 = format("SELECT * FROM %s;", TEST_DB_TABLE_NAME2);
    private static final String SHOW_TABLES_LIKE_CONTACTS = format("SHOW TABLES LIKE '%s'", TEST_DB_TABLE_NAME);
    private static Connection connection;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupOnce() throws IOException {
        connection = TestDBSetup.setupDbAndGetConnection(TEST_DB_NAME);
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        Common.close(connection);
    }

    // drops, creates, inserts data and saves all in one method: worst ever
    // shit, but tests are run independently - therefore - this is the only
    // way.
    @Test
    public void tableTest() throws Exception {

        final String name = "name";
        final String email = "name@email.com";
        final String phone = "(111) 111-1111";

        final String name2 = "name2";
        final String email2 = "name2@email.com";
        final String phone2 = "(222) 222-2222";

        final String newName = "newName";

        Contact contact = new Contact(0, name, email, phone);
        Contact contact2 = new Contact(0, name2, email2, phone2);
        DB db = new DB(connection);

        // try to simulate a potential work flow:
        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact);

            ResultSet resultSet = statement.executeQuery(SHOW_TABLES_LIKE_CONTACTS);
            if (resultSet.next()) {
                fail(format("database should not contain a %s table.", TEST_DB_TABLE_NAME));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.createTable(contact);

            ResultSet resultSet = statement.executeQuery(SHOW_TABLES_LIKE_CONTACTS);
            if (!resultSet.next()) {
                fail(format("database should contain a %s table.", TEST_DB_TABLE_NAME));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.save(contact);

            ResultSet resultSet = statement.executeQuery(SELECT_ALL_FROM_CONTACTS);

            if (!resultSet.next()) {
                fail("database should contain at least 1 row of contacts.");
            }

            assertEquals("after save() contact should have id.", 1, contact.getId());
            assertEquals(name, resultSet.getString("name"));
            assertEquals(email, resultSet.getString("email"));
            assertEquals(phone, resultSet.getString("phone"));

            db.save(contact2);

            ResultSet resultSet2 = statement.executeQuery(format(SELECT_FROM_CONTACTS_WHERE_ID_IS, contact2.getId()));

            if (!resultSet2.next()) {
                fail("database should contain a contact with id 2");
            }

            assertEquals(format("after save() %s should have id", TEST_DB_TABLE_NAME), 2, contact2.getId());
            assertEquals(name2, resultSet2.getString("name"));
            assertEquals(email2, resultSet2.getString("email"));
            assertEquals(phone2, resultSet2.getString("phone"));

            contact.setName(newName);

            db.save(contact);

            ResultSet resultSet3 = statement.executeQuery(format(SELECT_FROM_CONTACTS_WHERE_ID_IS, contact.getId()));

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

        final String name = "name";
        final String email = "name@email.com";
        final String phone = "(111) 111-1111";

        final String name2 = name + "2";
        final String email2 = email + "2";
        final String phone2 = "(222) 222-2222";

        final String name3 = name + "3";
        final String email3 = email + "3";
        final String phone3 = "(333) 333-3333";

        Contact2 contact1 = new Contact2(0, name, email, phone);
        Contact2 contact2 = new Contact2(0, name2, email2, phone2);
        Contact2 contact3 = new Contact2(0, name3, email3, phone3);
        DB db = new DB(connection);

        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact1);
            db.createTable(contact1);
            db.save(contact1);

            ResultSet resultSet = statement.executeQuery(format(SELECT_ALL_FROM_CONTACTS2));
            if (!resultSet.next()) {
                fail("database should contain a contact2 row.");
            }

            db.save(contact2);

            ResultSet resultSet2 = statement.executeQuery(format(SELECT_FROM_CONTACTS2_WHERE_ID_IS, contact2.getId()));
            if (!resultSet2.next()) {
                fail("database should contain a contact2 row.");
            }

            db.remove(contact1);

            ResultSet resultSet3 = statement.executeQuery(format(SELECT_FROM_CONTACTS2_WHERE_ID_IS, contact1.getId()));
            if (resultSet3.next()) {
                fail("database should not contain a row for contact 1.");
            }

            db.save(contact3);

            ResultSet resultSet4 = statement.executeQuery(format(SELECT_FROM_CONTACTS2_WHERE_ID_IS, contact3.getId()));
            if (!resultSet4.next()) {
                fail("database should contain a row for contact 3");
            }

            assertEquals(format("after save() %s should have id", TEST_DB_TABLE_NAME2), 3, contact3.getId());
            assertEquals(name3, resultSet4.getString("name"));
            assertEquals(email3, resultSet4.getString("email"));
            assertEquals(phone3, resultSet4.getString("phone"));

            db.remove(contact2);
            db.remove(contact3);

            ResultSet resultSet5 = statement.executeQuery(format(SELECT_ALL_FROM_CONTACTS2));
            if (resultSet5.next()) {
                fail("database should not contain any rows after removing all three test contacts.");
            }
        }

    }

}
