package mg.angular.db;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContactAppDbTest {

    // private static Connection connection;

    // TODO: add tests for db access

    @BeforeClass
    public static void setupOnce() throws IOException {
//        connection = TestDBSetup.setupDbAndGetConnection("angularjavatest",
//                                                         ANGULARJAVA_TEST_DB_DROP,
//                                                         ANGULARJAVA_TEST_DB_CREATE,
//                                                         ANGULARJAVA_TEST_DATA_INSERT);
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
//        Common.close(connection);
    }

    @Test
    public void findAllTest() {

//        ContactListDao contactListDao = new ContactListDao();

    }

}
