package mg.angular.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    public ContactResourceTest() {

        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
    }

    @Test
    public void testFilterSearch() {
        // XXX finish me!
        // contacts/<field1>
        // contacts/<field1> ... until all Contact fields covered.
        // reflection of object: contacts/{filterByFieldName} -> QueryParam("filterByFieldName")
    }

    @Test
    public void testFreeTextSearch() {
        // XXX finish me!
        // sort & q operation
    }

    @Ignore
    @Test
    public void testPostAndGetAll() throws JsonProcessingException, UnsupportedEncodingException {

        String name = "Functional Testname";
        String email = "functional@mail.com";
        String phone = "1234567";
        String name2 = getStringConcatenateWith2(name);
        String email2 = getStringConcatenateWith2(email);
        String phone2 = getStringConcatenateWith2(phone);

        ensureTestContactsExist(name, email, phone, name2, email2, phone2);

        String response = target(CONTACTS).request().get(String.class);

        boolean allMatch = Stream.of(name, email, phone).allMatch(response::contains);
        assertNotNull(response);
        assertTrue("response should have names, emails and phones of inserted test posts: ", allMatch);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ContactResource.class);
    }

    private void ensureTestContactsExist(String name, String email, String phone, String name2, String email2, String phone2) throws JsonProcessingException {

        boolean contactFound = findTestContact1(name, email, phone);
        boolean contact2Found = findTestContact2(name2, email2, phone2);

        if (!(contactFound && contact2Found)) {

            postTestContact1(name, email, phone);
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
