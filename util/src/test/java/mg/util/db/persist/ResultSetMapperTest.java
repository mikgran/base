package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.persist.support.Person;

public class ResultSetMapperTest {

    @BeforeClass
    public static void setupOnce() {
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // mvn -DfailIfNoTests=false -Dtest=ResultSetMapperTest#testMappingOne test
    @Test
    public void testMappingOne() throws SQLException, ResultSetMapperException, DBValidityException {

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
