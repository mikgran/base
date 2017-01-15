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
import org.junit.Ignore;
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

    // XXX finish me.

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
        // XXX finish me!
        // contacts/<field1>
        // contacts/<field1> ... until all Contact fields covered.
        // reflection of object: contacts/{filterByFieldName} -> QueryParam("filterByFieldName")
    }

    @Test
    public void testFreeTextSearch() throws IOException {
        // XXX finish me!
        // sort & q operation

        // sort parameters 1,2,3,4... match q parameters 1,2,3,4

        ensureTestContactsExist(name, email, phone, name2, email2, phone2);

        Response response = target(CONTACTS).queryParam("q", name).request().get();

        String json = response.readEntity(String.class);

        TypeReference<List<Contact>> typeReference = new TypeReference<List<Contact>>() {};
        List<Contact> contacts = mapper.readValue(json, typeReference);

        System.out.println("contacts:: " + contacts);

        assertNotNull(response);
        assertNotNull(contacts);
        assertEquals("there should be contacts: ", 1, contacts.size());
    }

    @Ignore
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

        boolean contactFound = findTestContact1(name, email, phone);
        boolean contact2Found = findTestContact2(name2, email2, phone2);

        if (!contactFound) {
            postTestContact1(name, email, phone);
        }

        if (!contact2Found) {
            postTestContact2(name2, email2, phone2);
        }
    }

    private boolean findTestContact1(String name, String email, String phone) {
        String response = target(CONTACTS).queryParam("sort", "name")
                                          .queryParam("q", name)
                                          .request()
                                          .get(String.class);

        boolean contactFound = Stream.of(name, email, phone)
                                     .allMatch(response::contains);
        return contactFound;
    }

    private boolean findTestContact2(String name2, String email2, String phone2) {
        String response;
        response = target(CONTACTS).queryParam("sort", "name")
                                   .queryParam("q", name2)
                                   .request()
                                   .get(String.class);

        boolean contact2Found = Stream.of(name2, email2, phone2)
                                      .allMatch(response::contains);
        return contact2Found;
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

    private void postTestContact1(String name, String email, String phone) throws JsonProcessingException {
        Contact contact = new Contact(null, name, email, phone);
        String contactJson = writer.writeValueAsString(contact);
        Response responseForPost = target(CONTACTS).request().post(Entity.json(contactJson));

        assertNotNull(responseForPost);
        assertEquals("posting new contact should return response: ", Response.Status.CREATED.getStatusCode(), responseForPost.getStatus());
    }

    private void postTestContact2(String name2, String email2, String phone2) throws JsonProcessingException {
        Contact contact2 = new Contact(null, name2, email2, phone2);
        String contactJson2 = writer.writeValueAsString(contact2);
        Response responseForPost2 = target(CONTACTS).request().post(Entity.json(contactJson2));

        assertNotNull(responseForPost2);
        assertEquals("posting new contact2 should return response: ", Response.Status.CREATED.getStatusCode(), responseForPost2.getStatus());
    }

}
