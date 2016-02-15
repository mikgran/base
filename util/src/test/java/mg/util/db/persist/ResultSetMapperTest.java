package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import mg.util.db.persist.proxy.ListProxy;
import mg.util.db.persist.support.Address2;
import mg.util.db.persist.support.Location5;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Person5;

public class ResultSetMapperTest {

    private static Connection connection;

    @BeforeClass
    public static void setupOnce() throws IOException, SQLException, DBValidityException {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");

        DB db = new DB(connection);

        Person5 person5 = new Person5();
        Location5 location5 = new Location5();

        db.createTable(person5);
        db.createTable(location5);
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Ignore
    @Test
    public void testLazyMapping() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        Person5 person5 = new Person5((Address2) null, "firstLazy1", "lastLazy2", Arrays.asList(new Location5("a lazyLoc5")));
        db.save(person5);

        long id = person5.getId();

        Person5 person5ById = new Person5();
        person5ById.setId(id);

        // db.setFetchPolicy(LAZY);
        Person5 personCandidate = db.findById(person5);

        System.out.println(personCandidate);

        assertNotNull(personCandidate);
        assertTrue("", personCandidate.getId() > 0);
        assertEquals("first name should be: ", "firstLazy1", personCandidate.getFirstName());
        assertEquals("last name should be: ", "lastLazy2", personCandidate.getLastName());
        assertNull(personCandidate.getAddress());
        List<Location5> locations = personCandidate.getLocations();
        assertNotNull(locations);

        Arrays.asList(locations.getClass().getGenericInterfaces()).stream().forEach(p -> System.out.println(p));

        assertTrue("locations should be an instance of ListProxy<?>: ", locations instanceof ListProxy<?>);

        // case ListProxy -> locations XXX
        assertEquals("the size of locations shoule be: ", 1, locations.size());
        assertTrue("there should be a location5 with id greater than 0: ", locations.get(0).getId() > 0);
        assertEquals("the person5id should be: ", person5.getId(), locations.get(0).getPersonsId());
        assertTrue("lazy loaded location5 should not have other fields than persondsId or id: ", !Common.hasContent(locations.get(0).getTodo()));

    }

    // mvn -DfailIfNoTests=false -Dtest=ResultSetMapperTest#testMappingOne test
    @Test
    public void testMappingOne() throws SQLException, DBMappingException, DBValidityException {

        ResultSet mockedResultSetForPersonFind = getMockedResultSetForPersonFind();

        Person person = new Person();
        SqlBuilder personBuilder = SqlBuilderFactory.of(person);

        ResultSetMapper<Person> personMapper = ResultSetMapper.of(person, personBuilder);

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
