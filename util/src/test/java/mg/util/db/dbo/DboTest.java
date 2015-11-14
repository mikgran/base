package mg.util.db.dbo;

import static java.lang.String.format;
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

    private static String TEST_DB_NAME = "dbotest";
    private static String TEST_DB_TABLE_NAME = "contacts";
    private static String SHOW_TABLES_LIKE_CONTACTS_QUERY = format("SHOW TABLES LIKE '%s'", TEST_DB_TABLE_NAME);
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

        Contact contact = new Contact("name", "name@email.com", "(111) 111-1111");
        Dbo<Contact> dbo = new Dbo<Contact>(connection, contact);

        try (Statement statement = connection.createStatement()) {

            dbo.dropTable();

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);
            if (resultSet.next()) {
                fail(format("database should not contain a %s table", TEST_DB_TABLE_NAME));
            }
        }

        try (Statement statement = connection.createStatement()) {

            dbo.createTable();

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);

            if (!resultSet.next()) {

                fail(format("database should contain a %s table", TEST_DB_TABLE_NAME));
            }
        }
        
        try (Statement statement = connection.createStatement()) {
            
            dbo.persist();
            // xxx
            querySelectAllFromContacts(statement);
        }
        

        try (Statement statement = connection.createStatement()) {

            dbo.dropTable();

            ResultSet resultSet = queryShowTablesLikeDboTest(statement);
            if (resultSet.next()) {
                fail(format("database should not contain a %s table", TEST_DB_TABLE_NAME));
            }
        }

    }

    private void querySelectAllFromContacts(Statement statement) {
        
        
        
    }

    private ResultSet queryShowTablesLikeDboTest(Statement statement) throws SQLException {

        statement.executeQuery(SHOW_TABLES_LIKE_CONTACTS_QUERY);
        return statement.getResultSet();
    }

}
