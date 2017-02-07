package mg.angular.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.angular.db.Contact;

// acceptance and-or functional tests, run only for coverage, not for unit testing, keep @Ignore tags on all methods when committing
public class ContactResourceTest extends JerseyTest {

    // FIXME finish me!

    // query test

    // format: ?q=free string -> sql constraint or filter?
    // consider creating a constraint & builder for all columns in table for a slow query

    // case getAll no parameters
    // case getAll sortQuery, by singular field
    // case getAll sortQuery, by two fields, which other descending direction

    private static final String CONTACTS = "contacts";
    private SimpleFilterProvider defaultFilterProvider;
    private ObjectMapper mapper;
    private ObjectWriter writer;
    private String name;
    private String email;
    private String phone;
    private String name2;
    private String email2;
    private String phone2;

    public ContactResourceTest() {

        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
        initTestData();
    }

    @Test
    public void testFilterSearch() {
        // FIXME finish me!
        // contacts/<field1>
        // contacts/<field1> ... until all Contact fields covered.
        // reflection of object: contacts/{filterByFieldName} -> QueryParam("filterByFieldName")
    }

    @Test
    public void testFreeTextSearch() throws IOException {
        // FIXME finish me!
        ensureTestContactsExist(name, email, phone, name2, email2, phone2);

        Response response = target(CONTACTS).queryParam("searchTerm", "name")
                                            .queryParam("q", name).request()
                                            .get();

        String json = response.readEntity(String.class);

        TypeReference<List<Contact>> typeReference = new TypeReference<List<Contact>>() {
        }; // funky class for carrying type.

        List<Contact> contacts = mapper.readValue(json, typeReference);

        assertNotNull(response);
        assertNotNull(contacts);
        assertEquals("there should be contacts: ", 1, contacts.size());
    }

    @Test
    public void testPostAndGetAll() throws JsonProcessingException, UnsupportedEncodingException {

        ensureTestContactsExist(name, email, phone, name2, email2, phone2);

        Response response = target(CONTACTS).request().get();
        String json = response.readEntity(String.class);

        assertNotNull(response);
        assertEquals("response code should be: ", Response.Status.OK.getStatusCode(), response.getStatus());
        boolean allMatch = Stream.of(name, email, phone, name2, email2, phone2)
                                 .allMatch(json::contains);
        assertTrue("response should have names, emails and phones of inserted test posts: ", allMatch);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ContactResource.class);
    }

    private void ensureTestContactsExist(String name, String email, String phone, String name2, String email2, String phone2) throws JsonProcessingException {

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
        String response = target(CONTACTS).queryParam("sort", "name")
                                          .queryParam("searchTerm", "name")
                                          .queryParam("q", name)
                                          .request()
                                          .get(String.class);

        boolean contactFound = Stream.of(name, email, phone)
                                     .allMatch(response::contains);
        return contactFound;
    }

    private String getStringConcatenateWith2(String s) {
        return s + "2";
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
        Contact contact = new Contact(null, name, email, phone);
        String contactJson = writer.writeValueAsString(contact);
        Response responseForPost = target(CONTACTS).request().post(Entity.json(contactJson));

        assertNotNull(responseForPost);
        assertEquals("posting new contact should return response: ", Response.Status.CREATED.getStatusCode(), responseForPost.getStatus());
    }

}
