package mg.util.db.dbo;

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

public class DboTest {

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

        Contact contact = new Contact(name, email, phone);
        Dbo<Contact> dbo = new Dbo<Contact>(connection, contact);

        try (Statement statement = connection.createStatement()) {

            dbo.dropTable();

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);
            if (resultSet.next()) {
                fail(format("database should not contain a %s table.", TEST_DB_TABLE_NAME));
            }
        }

        try (Statement statement = connection.createStatement()) {

            dbo.createTable();

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);

            if (!resultSet.next()) {

                fail(format("database should contain a %s table.", TEST_DB_TABLE_NAME));
            }
        }

        try (Statement statement = connection.createStatement()) {

            dbo.persist();

            ResultSet resultSet = querySelectAllFromContacts(statement);

            if (!resultSet.next()) {
                fail("database should contain at least 1 row of contacts.");
            }

            assertEquals(name, resultSet.getString("name"));
            assertEquals(email, resultSet.getString("email"));
            assertEquals(phone, resultSet.getString("phone"));
        }

        try (Statement statement = connection.createStatement()) {

            dbo.dropTable();

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
