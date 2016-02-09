package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.TestDBSetup;
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

        db.createTable(person5);
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLazyMapping() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        Person5 person5 = new Person5("firstLazy1", "lastLazy2");
        db.save(person5);

        person5.field("firstName").is("firstLazy1");

        db.setFetchPolicy(FetchPolicy.LAZY);
        Person5 personCandidate = db.findBy(person5);

        assertNotNull(personCandidate);
        assertTrue("", personCandidate.getId() > 0);
        assertEquals("first name should be: ", "firstLazy1", personCandidate.getFirstName());
        assertEquals("last name should be: ", "lastLazy2", personCandidate.getLastName());
        Address2 address = personCandidate.getAddress();
        assertNotNull(address);
        List<Location5> locations = personCandidate.getLocations();
        assertNotNull(locations);

        // case ListProxy -> locations TODO


    }

    // mvn -DfailIfNoTests=false -Dtest=ResultSetMapperTest#testMappingOne test
    @Test
    public void testMappingOne() throws SQLException, DBMappingException, DBValidityException {

        ResultSet mockedResultSetForPersonFind = getMockedResultSetForPersonFind();

        Person person = new Person();
        SqlBuilder personBuilder = SqlBuilder.of(person);

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
