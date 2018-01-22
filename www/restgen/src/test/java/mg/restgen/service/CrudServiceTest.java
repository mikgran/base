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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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
import mg.util.db.persist.Persistable;
import mg.util.functional.function.ThrowingFunction;

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
    private static RestGen restGen;

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

        restGen = RestGen.init();
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

    // @Disabled
    @Test
    public void testServiceGetAll() throws Exception {

        assertNotNull(crudService);

        String command = "get";

        restGen.register(Contact2.class, crudService, command);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("command", command);
        parameters.put("classRef", Contact2.class);

        List<ServiceResult> serviceResults;
        serviceResults = restGen.servicesFor(Contact2.class, command)
                                .map(si -> si.services)
                                .filter(Common::hasContent)
                                .getOrElseGet(() -> Collections.emptyList())
                                .stream()
                                .map(applyService(parameters, new Contact2())) // no fields set -> getAll
                                .collect(Collectors.toList());

        System.err.println("XX:: ");
        serviceResults.stream()
                      .forEach(System.err::println);

        List<Contact2> foundContacts = serviceResults.stream()
                                                     .map(sr -> sr.payload)
                                                     .filter(payload -> payload != null)
                                                     .map(asInstanceOf(Contact2.class))
                                                     .filter(c -> c != null)
                                                     .map(contact2 -> {
                                                         contact2.setId(0L);
                                                         return contact2;
                                                     }) // fetched Persistables return with id values: zero out the id.
                                                     .collect(Collectors.toList());


        boolean allFound = Stream.of(contact, contact2)
                                 .allMatch(foundContacts::contains);

        assertTrue(allFound, "payload should equal to: " +
                             contact +
                             " and " +
                             contact2);
    }

    @Test
    public void testServiceGetByFields() throws Exception {

        assertNotNull(crudService);

        String command = "get";

        restGen.register(Contact2.class, crudService, command);

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
            serviceResults = restGen.servicesFor(Contact2.class, command)
                                    .map(si -> si.services)
                                    .filter(Common::hasContent)
                                    .getOrElseGet(() -> Collections.emptyList())
                                    .stream()
                                    .map(applyService(parameters, target))
                                    .collect(Collectors.toList());

            // String expectedPayload = "{\"email\":\"email22\",\"id\":3,\"name\":\"name22\",\"phone\":\"1234567777\"}";
            Contact2 expectedContact = getTestContact2(0L, name2, email2, phone2); // getTestContact2

            boolean isPayloadFound = serviceResults.stream()
                                                   .map(sr -> sr.payload)
                                                   .filter(payload -> payload != null)
                                                   .map(asInstanceOf(Contact2.class))
                                                   .map(contact2 -> {
                                                       contact2.setId(0L);
                                                       return contact2;
                                                   }) // fetched Persistables return with id values: zero out the id.
                                                   .anyMatch(contact2 -> expectedContact.equals(contact2));

            assertTrue(isPayloadFound, "payload should equal to " + expectedContact);

        } catch (Exception e) {
            e.printStackTrace();
            fail("crudService.apply(testContact, parameters) should not produce an exception: " + e.getMessage());
        }
    }

    @Disabled // TOIMPROVE: finish this -> test coverage.
    @Test
    public void testServicePut2() {

        assertNotNull(crudService);

        String command = "put";

        restGen.register(Contact2.class, crudService, command);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("command", command);
        parameters.put("classRef", Contact2.class);

        String name2 = "name1";
        String email2 = "email1";
        String phone2 = "1234567";
        Contact2 testContact = getTestContact2(0L, name2, email2, phone2);

        try {
            Contact2 candidateContact2 = new Contact2(connection);
            candidateContact2.field("name").is(name2)
                             .and()
                             .field("email").is(email2)
                             .and()
                             .field("phone").is(phone2);

        } catch (Exception e) {
        }

    }

    private ThrowingFunction<RestService, ServiceResult, Exception> applyService(Map<String, Object> parameters, Persistable target) {
        return (ThrowingFunction<RestService, ServiceResult, Exception>) service -> service.apply(target, parameters);
    }

    private Contact2 getTestContact2(long id, String name, String email, String phone) {
        Contact2 testContact = new Contact2();
        testContact.setEmail(email)
                   .setName(name)
                   .setPhone(phone)
                   .setId(id);
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

}
