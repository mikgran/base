package mg.angular.rest;

import static org.junit.Assert.assertNotNull;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
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
    private ObjectWriter writer;
    private ObjectMapper mapper;

    public ContactResourceTest() {

        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
    }

    @Ignore
    @Test
    public void testGetAll() throws JsonProcessingException {

        Contact contact = new Contact(null, "Functional Testname", "functional@mail.com", "1234567");
        String contactJson = writer.writeValueAsString(contact);

        Response responseForPost = target(CONTACTS).request().post(Entity.json(contactJson));

        assertNotNull(responseForPost);
        Assert.assertEquals("posting new contact should return response: ", Response.Status.CREATED.getStatusCode(), responseForPost.getStatus());

        String response = target(CONTACTS).request().get(String.class);

        //assertNotNull();

        System.out.println("response::" + response);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ContactResource.class);
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
