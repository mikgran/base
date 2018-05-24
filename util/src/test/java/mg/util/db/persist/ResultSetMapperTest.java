package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.support.Address2;
import mg.util.db.persist.support.Location5;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Person5;

public class ResultSetMapperTest {

    private static Connection connection;

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupOnce() throws IOException, SQLException, DBValidityException {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");

        DB db = new DB(connection);

        Address2 address2 = new Address2();
        Person5 person5 = new Person5();
        Location5 location5 = new Location5();

        db.createTable(person5);
        db.createTable(address2);
        db.createTable(location5);
    }

    @AfterClass
    public static void tearDownOnce() {
        Common.close(connection);
    }

    // TOIMPROVE: change to use unique test classes, so that cleanup is possible
    @Ignore
    @Test
    public void testLazyMapping() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        Person5 person5 = new Person5(new Address2("an-address1"), "firstLazy1", "lastLazy2", Arrays.asList(new Location5("a lazyLoc5")));
        db.save(person5);

        long id = person5.getId();

        Person5 person5ById = new Person5();
        person5ById.setAddress(new Address2());
        person5ById.setLocations(Arrays.asList(new Location5()));
        person5ById.setId(id);

        db.setFetchPolicy(FetchPolicy.LAZY);
        Person5 personCandidate = db.findById(person5ById);
        assertNotNull(personCandidate);
        assertTrue("personCandidate should have an id over 0.", personCandidate.getId() > 0);
        assertEquals("first name should be: ", "firstLazy1", personCandidate.getFirstName());
        assertEquals("last name should be: ", "lastLazy2", personCandidate.getLastName());

        List<Location5> locations = personCandidate.getLocations();
        assertNotNull(locations);
        assertTrue("locations should be a proxy.", locations.getClass().getName().contains("ByteBuddy"));
        assertEquals("the size of locations should be: ", 1, locations.size());
        assertTrue("there should be a location5 with id greater than 0: ", locations.get(0).getId() > 0);
        assertEquals("the location5 should contain person5id: ", person5.getId(), locations.get(0).getPersonsId());
        assertEquals("the location5 should contain location named: ", "a lazyLoc5", locations.get(0).getLocation());

        Address2 address2 = personCandidate.getAddress();
        assertNotNull(address2);
        assertTrue("address2 should be a proxy.", address2.getClass().getName().contains("ByteBuddy"));
        assertTrue("there should be a address2 with id greater than 0: ", address2.getId() > 0);
        assertEquals("the address2.address should be: ", "an-address1", address2.getAddress());
        assertEquals("the address2 should contain person5id: ", person5.getId(), address2.getPersonsId());
    }

    // mvn -DfailIfNoTests=false -Dtest=ResultSetMapperTest#testMappingOne test
    @Test
    public void testMappingOne() throws SQLException, DBMappingException, DBValidityException {

        ResultSet mockedResultSetForPersonFind = getMockedResultSetForPersonFind();

        Person person = new Person();
        SqlBuilder personBuilder = SqlBuilderFactory.of(person);

        ResultSetMapper<Person> personMapper = ResultSetMapperFactory.of(person, personBuilder);

        Person mappedPerson = personMapper.mapOne(mockedResultSetForPersonFind);

        assertNotNull(mappedPerson);
        assertEquals("id should be: ", 1, mappedPerson.getId());
        assertEquals("first name should be: ", "firstNameX", mappedPerson.getFirstName());
        assertEquals("last name should be: ", "lastNameY", mappedPerson.getLastName());
        assertEquals("todos size should be should be: ", 0, mappedPerson.getTodos().size());
    }

    private ResultSet getMockedResultSetForPersonFind() throws SQLException {
        ResultSet resultSet = context.mock(ResultSet.class);

        context.checking(new Expectations() {
            {
                oneOf(resultSet).next(); // advance to first row Person
                will(returnValue(true));

                oneOf(resultSet).isClosed();
                will(returnValue(false));

                oneOf(resultSet).getObject("p1.id");
                will(returnValue((long) 1));

                // oneOf(resultSet).getObject("id");
                // will(returnValue((long) 1));

                // oneOf(resultSet).getLong("id");
                // will(returnValue(1));

                oneOf(resultSet).getObject("p1.firstName");
                will(returnValue("firstNameX"));

                // oneOf(resultSet).getObject("firstName");
                // will(returnValue("firstNameX"));

                oneOf(resultSet).getObject("p1.lastName");
                will(returnValue("lastNameY"));
            }
        });
        return resultSet;
    }

}
