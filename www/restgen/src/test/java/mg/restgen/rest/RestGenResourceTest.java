package mg.restgen.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.db.Contact;

// acceptance and-or functional tests, run only for coverage, not for unit testing, keep @Ignore/@Disabled tags on all methods when committing
// TOIMPROVE: find a way for the tests work with Gradle -> it hangs with JerseyTests, while
// the maven install works just fine.
@TestInstance(Lifecycle.PER_CLASS)
public class RestGenResourceTest extends JerseyTest {

    private final String RESOURCE_NAME = "restgen/id/contacts";
    private SimpleFilterProvider defaultFilterProvider;
    private ObjectMapper mapper;
    private ObjectWriter writer;
    private String name;
    private String email;
    private String phone;
    private String name2;
    private String email2;
    private String phone2;

    private static String getStringConcatenateWith2(String s) {
        return s + "2";
    }

    public RestGenResourceTest() {
        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
        initTestData();
    }

    // JerseyTest && JUnit5 fix, remove when supported by JUnit5
    // do not name this tearDown()
    @AfterAll
    public void after() throws Exception {
        super.tearDown();
    }

    // JerseyTest && JUnit5 fix, remove when supported by JUnit5
    // do not name this setup()
    @BeforeAll
    public void before() throws Exception {
        super.setUp();
    }

    @Disabled
    @Test
    public void testFilterSearch() {
        // FIXME: finish me! Create field filters
        // contacts/<field1>
        // contacts/<field1> ... until all Contact fields covered.
        // reflection of object: contacts/{filterByFieldName} -> QueryParam("filterByFieldName")
    }

    @Disabled
    @Test
    public void testFreeTextSearch() throws IOException {

        ensureTestContactsExist(name, email, phone, name2, email2, phone2);

        // TOIMPROVE: currently the free text search uses field names with AND joins, improve by field1=q1 OR field3=q1 OR field2=q2
        Response response = target(RESOURCE_NAME).queryParam("searchTerm", "name")
                                                 .queryParam("q", name).request()
                                                 .get();

        String json = response.readEntity(String.class);

        // dynamic way of constructing a List of MyClass (can replace Contact.class with classObj.getClass();)
        List<Contact> contacts = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Contact.class));

        assertNotNull(response);
        assertNotNull(contacts);
        assertEquals(1, contacts.size(), "there should be contacts: ");
    }

    // saveContact() && getAll()
    //@Ignore
    @Test
    public void testPostAndGetAll() throws Exception {

        ensureTestContactsExist(name, email, phone, name2, email2, phone2);

        Response response = target(RESOURCE_NAME).request().get();
        String json = response.readEntity(String.class);

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "response code should be: ");
        boolean allMatch = Stream.of(name, email, phone, name2, email2, phone2)
                                 .allMatch(json::contains);
        assertTrue(allMatch, "response should have names, emails and phones of inserted test posts: ");

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(RestGenResource.class);
    }

    private void ensureTestContactsExist(String name, String email, String phone, String name2, String email2, String phone2) throws JsonProcessingException {

        // Stream.of(name, email, phone, name2, email2, phone2)
        //       .forEach(System.err::println);

        boolean contactFound = findTestContact(name, email, phone);
        boolean contact2Found = findTestContact(name2, email2, phone2);

        if (!contactFound) {
            postTestContact(name, email, phone);
        }

        if (!contact2Found) {
            postTestContact(name2, email2, phone2);
        }
    }

    private boolean findTestContact(String name, String email, String phone) {
        String response = target(RESOURCE_NAME)
                                               .queryParam("sort", "name")
                                               .queryParam("searchTerm", "name")
                                               .queryParam("q", name)
                                               .request()
                                               .get(String.class);

        boolean contactFound = Stream.of(name, email, phone)
                                     .allMatch(response::contains);
        return contactFound;
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

    private void initTestData() {
        name = "__Functional Testname";
        email = "__functional@mail.com";
        phone = "__1234567";
        name2 = getStringConcatenateWith2(name);
        email2 = getStringConcatenateWith2(email);
        phone2 = getStringConcatenateWith2(phone);
    }

    private void postTestContact(String name, String email, String phone) throws JsonProcessingException {
        Contact contact = new Contact(0L, name, email, phone);
        String contactJson = writer.writeValueAsString(contact);
        Response responseForPost = target(RESOURCE_NAME).request().post(Entity.json(contactJson));

        assertNotNull(responseForPost);
        assertEquals(Response.Status.CREATED.getStatusCode(), responseForPost.getStatus(), "posting new contact should return response: ");

        System.err.println("return entity body == '" + responseForPost.getLocation() + "','" + responseForPost.readEntity(String.class) + "'");
    }

}
