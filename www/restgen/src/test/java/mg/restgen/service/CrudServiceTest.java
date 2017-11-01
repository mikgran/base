package mg.restgen.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mg.restgen.db.Contact2;
import mg.util.Common;
import mg.util.TestConfig;
import mg.util.db.DBConfig;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;

public class CrudServiceTest {

    public static final String dbName = "restgentest";
    public static final String EMAIL = "__test.name@email.com";
    public static final String NAME = "__Test Name";
    public static final String PHONE = "__(111) 111-1111";
    public static final String PHONE_123_4567 = "__123 4567";
    public static final String TESTEY_TESTFUL = "__Testey Testful";
    public static final String TESTEY_TESTFUL_AT_MAIL_DOT_COM = "__testey.testful@mail.com";
    public static final Contact2 contact = new Contact2(0L, NAME, EMAIL, PHONE);
    public static final Contact2 contact2 = new Contact2(0L, NAME + "2", EMAIL + "2", PHONE + "2");

    private static Connection connection;
    private static CrudService crudService;
    private static boolean isServiceCacheInitDone;

    @BeforeAll
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection(dbName);

        DB db = new DB(connection);
        db.dropTable(contact);
        db.createTable(contact);
        db.save(contact);
        db.save(contact2);

        crudService = new CrudService(new DBConfig(new TestConfig()));
    }

    @AfterAll
    public static void tearDownOnce() throws SQLException {
        Common.close(connection);
    }

    public synchronized void initTestServiceCache() {
        if (isServiceCacheInitDone) {
            return;
        }
        // ensure called at least once: fire the constructor of TestServiceCache only.
        @SuppressWarnings("unused")
        TestServiceCache testServiceCache = new TestServiceCache();
        isServiceCacheInitDone = true;
    }

    // @Disabled
    @Test
    public void testServicePut() throws Exception {

        initTestServiceCache();

        assertNotNull(crudService);

        TestServiceCache.register(Contact2.class, crudService);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("command", "put");
        // parameters.put("nameRef", "contact2"); // nameRef == id

        String name2 = "name1";
        String email2 = "email1";
        String phone2 = "1234567";
        Contact2 testContact = new Contact2();
        testContact.setEmail(email2);
        testContact.setName(name2);
        testContact.setPhone(phone2);

        try {
            // the beef !
            crudService.apply(testContact, parameters);

            Contact2 candidateContact2 = new Contact2(connection);
            candidateContact2.field("name").is(name2)
                             .and()
                             .field("email").is(email2)
                             .and()
                             .field("phone").is(phone2);

            Contact2 contact2Fetched = candidateContact2.find();

            assertNotNull(contact2Fetched);

        } catch (Exception e) {
            fail("crudService.apply(testContact, parameters) should not produce an exception: " + e.getMessage());
        }

    }

    class TestServiceCache extends ServiceCache {

        public TestServiceCache() {
            services = new ConcurrentHashMap<>(); // replace the existing ConcurrenHashMap
        }
    }

}
