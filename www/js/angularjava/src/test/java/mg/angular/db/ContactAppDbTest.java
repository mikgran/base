package mg.angular.db;

import static mg.util.Common.close;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.db.persist.DBValidityException;

public class ContactAppDbTest {

    private static Connection connection;

    public static final String NAME = "Test Name";
    public static final String EMAIL = "test.name@email.com";
    public static final String PHONE = "(111) 111-1111";
    public static Contact contact = new Contact(0, NAME, EMAIL, PHONE);

    // TODO: angular: add tests for db access
    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("angularjavatest");

        DB db = new DB(connection);
        db.dropTable(contact);
        db.createTable(contact);
        db.save(contact);
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        close(connection);
    }

    @Test
    public void findAllTest() throws DBValidityException, SQLException {

        // ContactListDao contactListDao = new ContactListDao();

        // contactListDao.findAll();

    }

}
