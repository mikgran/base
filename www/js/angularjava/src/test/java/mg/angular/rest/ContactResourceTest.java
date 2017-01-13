package mg.angular.rest;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

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

    @Ignore
    @Test
    public void testGetAll() throws JsonProcessingException, UnsupportedEncodingException {

        //        String name = "Functional Testname";
        //        String email = "functional@mail.com";
        //        String phone = "1234567";
        //        String name2 = getStringConcatenateWith2(name);
        //        String email2 = getStringConcatenateWith2(email);
        //        String phone2 = getStringConcatenateWith2(phone);

        target(CONTACTS).queryParam("q", "Functional Testname").request().get(String.class);

        //        Contact contact = new Contact(null, name, email, phone);
        //        String contactJson = writer.writeValueAsString(contact);
        //        Response responseForPost = target(CONTACTS).request().post(Entity.json(contactJson));
        //
        //        assertNotNull(responseForPost);
        //        Assert.assertEquals("posting new contact should return response: ", Response.Status.CREATED.getStatusCode(), responseForPost.getStatus());
        //
        //        Contact contact2 = new Contact(null, name2, email2, phone2);
        //        String contactJson2 = writer.writeValueAsString(contact2);
        //        Response responseForPost2 = target(CONTACTS).request().post(Entity.json(contactJson2));
        //
        //        assertNotNull(responseForPost2);
        //        Assert.assertEquals("posting new contact2 should return response: ", Response.Status.CREATED.getStatusCode(), responseForPost2.getStatus());
        //
        //        String response = target(CONTACTS).request().get(String.class);
        //
        //        boolean allMatch = Stream.of(name, email, phone).allMatch(response::contains);
        //        assertNotNull(response);
        //        assertTrue("response should have names, emails and phones of inserted test posts: ", allMatch);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ContactResource.class);
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

}
