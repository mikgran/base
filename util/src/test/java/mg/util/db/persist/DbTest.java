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
    private static final String SELECT_ALL_FROM_CONTACTS_QUERY = format("SELECT * FROM %s;", TEST_DB_TABLE_NAME);
    private static final String SHOW_TABLES_LIKE_CONTACTS_QUERY = format("SHOW TABLES LIKE '%s'", TEST_DB_TABLE_NAME);
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

    // drops, creates, inserts data, drops all in one method: worst ever shit,
    // but tests are run independently - therefore - this is the only way.
    @Test
    public void tableTest() throws Exception {

        final String name = "name";
        final String email = "name@email.com";
        final String phone = "(111) 111-1111";

        final String name2 = "name2";
        final String email2 = "name2@email.com";
        final String phone2 = "(222) 222-2222";

        Contact contact = new Contact(0, name, email, phone);
        Contact contact2 = new Contact(0, name2, email2, phone2);
        DB<Contact> db = new DB<Contact>(connection);

        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact);

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);
            if (resultSet.next()) {
                fail(format("database should not contain a %s table.", TEST_DB_TABLE_NAME));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.createTable(contact);

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);
            if (!resultSet.next()) {
                fail(format("database should contain a %s table.", TEST_DB_TABLE_NAME));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.save(contact);

            ResultSet resultSet = querySelectAllFromContacts(statement);

            if (!resultSet.next()) {
                fail("database should contain at least 1 row of contacts.");
            }

//            assertEquals("after save() contact should have id ", 1, contact.getId());
            assertEquals(name, resultSet.getString("name"));
            assertEquals(email, resultSet.getString("email"));
            assertEquals(phone, resultSet.getString("phone"));
            
//            db.save(contact2);
//            
//            assertEquals("after save() contact2 should have id", 2, contact2.getId());
//
//            ResultSet resultSet2 = statement.executeQuery(format("SELECT * FROM contacts where id = %s;", contact2.getId()));
//
//            if (!resultSet2.next()) {
//                fail("database should contain a contact with id " + contact2.getId());
//            }
//
//            assertEquals(name2, resultSet.getString("name"));
//            assertEquals(email2, resultSet.getString("email"));
//            assertEquals(phone2, resultSet.getString("phone"));
        }

        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact);

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);
            if (resultSet.next()) {
                fail(format("database should not contain a %s table.", TEST_DB_TABLE_NAME));
            }
        }

    }

    private ResultSet querySelectAllFromContacts(Statement statement) throws SQLException {

        return statement.executeQuery(SELECT_ALL_FROM_CONTACTS_QUERY);
    }

    private ResultSet queryShowTablesLikeDboTest(Statement statement) throws SQLException {

        return statement.executeQuery(SHOW_TABLES_LIKE_CONTACTS_QUERY);
    }

}
