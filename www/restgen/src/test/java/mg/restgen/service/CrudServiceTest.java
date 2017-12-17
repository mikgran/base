package mg.restgen.service;

import static mg.util.Common.asInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.db.Contact2;
import mg.restgen.rest.CustomAnnotationIntrospector;
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
    private static TestServiceCache testServiceCache;

    private SimpleFilterProvider defaultFilterProvider;
    private ObjectMapper mapper;
    private ObjectWriter writer;

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

    public CrudServiceTest() {
        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
    }

    public synchronized void initTestServiceCache() {
        if (testServiceCache != null) {
            return;
        }
        testServiceCache = new TestServiceCache();
    }

    @Test
    public void testServiceGet() {

        initTestServiceCache();

        assertNotNull(crudService);

        String command = "get";

        TestServiceCache.register(Contact2.class, crudService, command);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("command", command);
        parameters.put("classRef", Contact2.class);

        String name2 = "name22";
        String email2 = "email22";
        String phone2 = "1234567777";

        Contact2 target = getTestContact2(0L, name2, email2, phone2);

        try {
            target.setConnectionAndDB(connection);
            target.save();
            target.setId(0L);
            target.field("name").is(name2)
                  .field("email").is(email2)
                  .field("phone").is(phone2);

            List<ServiceResult> serviceResults;
            serviceResults = TestServiceCache.servicesFor(Contact2.class, command)
                                             .map(si -> si.services)
                                             .filter(Common::hasContent)
                                             .orElseGet(() -> Collections.emptyList())
                                             .stream()
                                             .map(service -> service.apply(target, parameters))
                                             .collect(Collectors.toList());

            // String expectedPayload = "{\"email\":\"email22\",\"id\":3,\"name\":\"name22\",\"phone\":\"1234567777\"}";
            Contact2 expectedContact = getTestContact2(0L, name2, email2, phone2); // getTestContact2

            boolean isPayloadFound = serviceResults.stream()
                                                   .map(sr -> sr.payload)
                                                   .filter(payload -> payload != null)
                                                   .map(asInstanceOf(Contact2.class))
                                                   .map(contact2 -> contact2.setId(0L)) // fetched Persistables return with id values: zero out the id.
                                                   .anyMatch(contact2 -> contact2.equals(expectedContact));

            assertTrue(isPayloadFound, "payload should equal to " + expectedContact);

        } catch (Exception e) {
            fail("crudService.apply(testContact, parameters) should not produce an exception: " + e.getMessage());
        }
    }

    @Test
    public void testServicePut() throws Exception {

        initTestServiceCache();

        assertNotNull(crudService);

        String command = "put";

        TestServiceCache.register(Contact2.class, crudService, command);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("command", command);
        parameters.put("classRef", Contact2.class);

        String name2 = "name1";
        String email2 = "email1";
        String phone2 = "1234567";
        Contact2 testContact = getTestContact2(0L, name2, email2, phone2);

        try {

            Optional<ServiceInfo> serviceInfo = TestServiceCache.servicesFor(Contact2.class, command);

            // the beef !
            serviceInfo.map(si -> si.services)
                       .filter(Common::hasContent)
                       .orElseGet(() -> Collections.emptyList())
                       .stream()
                       .map(service -> service.apply(testContact, parameters))
                       .collect(Collectors.toList());

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

    private Contact2 getTestContact2(long id, String name, String email, String phone) {
        Contact2 testContact = new Contact2();
        testContact.setId(id)
                   .setEmail(email)
                   .setName(name)
                   .setPhone(phone);
        return testContact;
    }

    private void initDefaultFilterProvider() {
        defaultFilterProvider = new SimpleFilterProvider();
        defaultFilterProvider.setFailOnUnknownId(false);
    }

    private void initDefaultWriter() {
        writer = mapper.writer(defaultFilterProvider);
    }

    private void initMapper() {
        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }

    class TestServiceCache extends RestGen {

        public TestServiceCache() {
            serviceInfos = new ConcurrentHashMap<>(); // replace the existing ConcurrenHashMap
        }
    }

}
