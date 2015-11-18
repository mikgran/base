package mg.angular.db;

import static mg.util.Common.close;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.db.persist.DbValidityException;

public class ContactAppDbTest {

    private static Connection connection;

    public static final String name = "Test Name";
    public static final String email = "test.name@email.com";
    public static final String phone = "(111) 111-1111";
    public static Contact contact = new Contact(0, name, email, phone);

    // TODO: add tests for db access

    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("angularjavatest");
        
        DB<Contact> dbo = new DB<Contact>(connection);
        dbo.dropTable(contact);
        dbo.createTable(contact);
        dbo.save(contact);
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        close(connection);
    }

    @Test
    public void findAllTest() throws DbValidityException, SQLException {

        // ContactListDao contactListDao = new ContactListDao();
        
        // contactListDao.findAll();
        
        

    }

}
