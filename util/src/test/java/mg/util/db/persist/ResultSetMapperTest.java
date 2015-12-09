package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.persist.support.Person;

public class ResultSetMapperTest {

//    private static Contact contact;
//    private static Person person;

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupOnce() {
//        contact = new Contact(1, "name1", "name1@mail.com", "(111) 111-1111");
//        person = new Person("firstName1", "lastName2",
//                            Arrays.asList(new Todo("1st", Collections.emptyList()),
//                                          new Todo("2nd", Collections.emptyList())));
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    // mvn -DfailIfNoTests=false -Dtest=ResultSetMapperTest#testMappingOne test
    @Ignore
    @Test
    public void testMappingOne() throws SQLException, ResultSetMapperException {

        ResultSet mockedResultSetForPersonFind = getMockedResultSetForPersonFind();

        ResultSetMapper<Person> resultSetMapper = new ResultSetMapper<Person>(new Person());

        Person mappedPerson = resultSetMapper.mapOne(mockedResultSetForPersonFind);

        assertNotNull(mappedPerson);
        assertEquals("id should be: ", 1, mappedPerson.getId());
        assertEquals("first name should be: ", "first name1", mappedPerson.getFirstName());
        assertEquals("last name should be: ", "last name2", mappedPerson.getLastName());
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
            }
        });
        return resultSet;
    }

}
