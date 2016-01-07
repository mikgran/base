package mg.util.db.persist;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.support.Contact;
import mg.util.db.persist.support.Contact2;
import mg.util.db.persist.support.Contact4;
import mg.util.db.persist.support.Location;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Person2;
import mg.util.db.persist.support.Person3;
import mg.util.db.persist.support.Todo;
import mg.util.db.persist.support.Todo2;
import mg.util.db.persist.support.Todo3;
import mg.util.functional.consumer.ThrowingConsumer;

public class DbTest {

    private static Connection connection;

    @BeforeClass
    public static void setupOnce() throws IOException, SQLException, DBValidityException {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");

        DB db = new DB(connection);

        Person person = new Person();
        Person2 person2 = new Person2("first111", "last222");
        Person3 person3 = new Person3();
        Todo todo = new Todo();
        Todo2 todo2 = new Todo2();
        Todo3 todo3 = new Todo3();

        db.dropTable(todo);
        db.dropTable(todo2);
        db.dropTable(todo3);
        db.dropTable(person);
        db.dropTable(person2);
        db.dropTable(person3);

        db.createTable(person);
        db.createTable(person2);
        db.createTable(person3);
        db.createTable(todo);
        db.createTable(todo2);
        db.createTable(todo3);
        db.save(person2);
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException, DBValidityException {

        Contact contact = new Contact();
        Contact2 contact2 = new Contact2();
        Contact4 contact4 = new Contact4();
        Person person = new Person();
        Person2 person2 = new Person2();
        Person3 person3 = new Person3();
        Location location = new Location();
        Todo todo = new Todo();
        Todo2 todo2 = new Todo2();
        Todo3 todo3 = new Todo3();

        DB db = new DB(connection);
        db.dropTable(location);
        db.dropTable(contact);
        db.dropTable(contact2);
        db.dropTable(contact4);
        db.dropTable(todo);
        db.dropTable(todo2);
        db.dropTable(todo3);
        db.dropTable(person);
        db.dropTable(person2);
        db.dropTable(person3);

        Common.close(connection);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void tableTestDropsCreates() throws Exception {

        DB db = new DB(connection);

        Location location = new Location("place1");
        Location location2 = new Location("place2");
        Todo todo = new Todo("something todo", Collections.emptyList());
        Todo todo2 = new Todo("else todo", Arrays.asList(location, location2));
        Person person = new Person(0, "first1", "last1", Arrays.asList(todo, todo2));

        try {
            db.dropTable(location);
            db.dropTable(todo);
            db.dropTable(person);
            db.createTable(person);
            db.createTable(todo);
            db.createTable(location);

            // composition: perform a cascade update on all collections that are
            // tagged with collection annotations

            db.save(person);

        } catch (SQLException e) {
            fail("SQLException while create & drop: code: " + e.getErrorCode() + " message: " + e.getMessage());

        }

        try (Statement statement = connection.createStatement()) {

            assertThatAtLeastOneRowExists(statement, format("SELECT * FROM %s;", "persons"), "persons");
            assertThatAtLeastOneRowExists(statement, format("SELECT * FROM %s;", "todos"), "todos");
        }

        // TOIMPROVE: add test coverage; test for inserted composition contents - even though implicitly
        // tested by other tests.
    }

    @Test
    public void tableTestSavesRemoves() throws Exception {

        String name = "name";
        String email = "name@email.com";
        String phone = "(111) 111-1111";

        String name2 = name + "2";
        String email2 = email + "2";
        String phone2 = "(222) 222-2222";

        String name3 = name + "3";
        String email3 = email + "3";
        String phone3 = "(333) 333-3333";

        Contact2 contact1 = new Contact2(0, name, email, phone);
        Contact2 contact2 = new Contact2(0, name2, email2, phone2);
        Contact2 contact3 = new Contact2(0, name3, email3, phone3);
        DB db = new DB(connection);

        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact1);
            db.createTable(contact1);
            db.save(contact1);

            ResultSet resultSet = statement.executeQuery(format(format("SELECT * FROM %s;", "contacts2")));
            if (!resultSet.next()) {
                fail("database should contain a row for contact 1.");
            }

            db.save(contact2);

            ResultSet resultSet2 = statement.executeQuery(format("SELECT * FROM contacts2 WHERE id = %d;", contact2.getId()));
            if (!resultSet2.next()) {
                fail("database should contain a row for contact 2.");
            }

            db.remove(contact1);

            ResultSet resultSet3 = statement.executeQuery(format("SELECT * FROM contacts2 WHERE id = %d;", contact1.getId()));
            if (resultSet3.next()) {
                fail("database should not contain a row for contact 1.");
            }

            db.save(contact3);

            ResultSet resultSet4 = statement.executeQuery(format("SELECT * FROM contacts2 WHERE id = %d;", contact3.getId()));
            if (!resultSet4.next()) {
                fail("database should contain a row for contact 3");
            }

            assertEquals(format("after save() %s should have id", "contacts2"), 3, contact3.getId());
            assertEquals(name3, resultSet4.getString("name"));
            assertEquals(email3, resultSet4.getString("email"));
            assertEquals(phone3, resultSet4.getString("phone"));

            db.remove(contact2);
            db.remove(contact3);

            ResultSet resultSet5 = statement.executeQuery(format(format("SELECT * FROM %s;", "contacts2")));
            if (resultSet5.next()) {
                fail("database should not contain any rows after removing all three test contacts.");
            }
        }
    }

    // drops, creates, inserts data and saves all in one method: worst ever
    // shit, but tests are run independently - therefore - this is the only
    // way.
    @Test
    public void tableTestUpdate() throws Exception {

        String name = "name";
        String email = "name@email.com";
        String phone = "(111) 111-1111";

        String name2 = "name2";
        String email2 = "name2@email.com";
        String phone2 = "(222) 222-2222";

        String newName = "newName";

        Contact contact = new Contact(0, name, email, phone);
        Contact contact2 = new Contact(0, name2, email2, phone2);
        DB db = new DB(connection);

        // try to simulate a potential work flow:
        try (Statement statement = connection.createStatement()) {

            db.dropTable(contact);

            ResultSet resultSet = statement.executeQuery(format("SHOW TABLES LIKE '%s'", "contacts"));
            if (resultSet.next()) {
                fail(format("database should not contain a %s table.", "contacts"));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.createTable(contact);

            ResultSet resultSet = statement.executeQuery(format("SHOW TABLES LIKE '%s'", "contacts"));
            if (!resultSet.next()) {
                fail(format("database should contain a %s table.", "contacts"));
            }
        }

        try (Statement statement = connection.createStatement()) {

            db.save(contact);

            ResultSet resultSet = statement.executeQuery(format("SELECT * FROM %s;", "contacts"));

            if (!resultSet.next()) {
                fail("database should contain at least 1 row of contacts.");
            }

            assertEquals("after save() contact should have id.", 1, contact.getId());
            assertEquals(name, resultSet.getString("name"));
            assertEquals(email, resultSet.getString("email"));
            assertEquals(phone, resultSet.getString("phone"));

            db.save(contact2);

            ResultSet resultSet2 = statement.executeQuery(format("SELECT * FROM contacts WHERE id = %d;", contact2.getId()));

            if (!resultSet2.next()) {
                fail("database should contain a contact with id 2");
            }

            assertEquals(format("after save() %s should have id", "contacts"), 2, contact2.getId());
            assertEquals(name2, resultSet2.getString("name"));
            assertEquals(email2, resultSet2.getString("email"));
            assertEquals(phone2, resultSet2.getString("phone"));

            contact.setName(newName);

            db.save(contact);

            ResultSet resultSet3 = statement.executeQuery(format("SELECT * FROM contacts WHERE id = %d;", contact.getId()));

            if (!resultSet3.next()) {
                fail("selecting all for id 1 should return contact.");
            }

            assertEquals("after name change to the newName should be:", newName, resultSet3.getString("name"));
            assertEquals(email, resultSet3.getString("email"));
            assertEquals(phone, resultSet3.getString("phone"));
        }
    }

    @Test
    public void testFindAllBy() throws SQLException, DBValidityException, ResultSetMapperException {

        DB db = new DB(connection);

        List<Person2> testValues = Arrays.asList(new Person2("test1", "value2"),
                                                 new Person2("testa", "value3"),
                                                 new Person2("test222", "value4"));

        testValues.stream()
                  .forEach((ThrowingConsumer<Persistable, Exception>) p -> db.save(p));

        Person2 person = new Person2();
        person.field("firstName")
              .like("te%");

        List<Person2> personCandidates = db.findAllBy(person);

        assertNotNull(personCandidates);
        assertEquals("the personCandidates list should contain persons: ", 3, personCandidates.size());
        assertPerson2EqualsAtIndex(personCandidates, 0, "test1", "value2");
        assertPerson2EqualsAtIndex(personCandidates, 1, "testa", "value3");
        assertPerson2EqualsAtIndex(personCandidates, 2, "test222", "value4");

        person.clearConstraints();
        person.field("firstName")
              .like("notFoundInDatabase");

        List<Person2> personCandidates2 = db.findAllBy(person);

        assertNotNull(personCandidates2);
        assertEquals("the personCandidates list should not contain persons: ", 0, personCandidates2.size());

    }

    @Test
    public void testFindAllByJoin() throws SQLException, DBValidityException, ResultSetMapperException {

        DB db = new DB(connection);

        List<Person3> testValues = asList(new Person3("test1", "value2", asList(new Todo3("to-do-1"),
                                                                                new Todo3("to-do-2"))),
                                          new Person3("testa", "value3"),
                                          new Person3("test222", "value4"));

        testValues.stream()
                  .forEach((ThrowingConsumer<Persistable, Exception>) p -> db.save(p));

        //        long id = testValues.get(0).getId();
        //
        //        System.out.println("id: " + id);

        //        List<Persistable> testValues2 = Arrays.asList(new Todo2("to-do-1", id),
        //                                                      new Todo2("to-do-2", id));

        //        try (Statement statement = connection.createStatement()) {
        //
        //            String insertIntoPersonsSql = "INSERT INTO persons3 (firstName, lastName) VALUES " +
        //                                          "('test1','value2')," +
        //                                          "('testa','value3')," +
        //                                          "('test222','value4')" +
        //                                          ";";
        //
        //            String insertIntoTodosSql = "INSERT INTO todos2 (todo) VALUES " +
        //                                        "('to-do-1')," +
        //                                        "('to-do-2')" +
        //                                        ";";
        //
        //            if (statement.executeUpdate(insertIntoPersonsSql) == 0 ||
        //                statement.executeUpdate(insertIntoTodosSql) == 0) {
        //                fail("insert into should not fail.");
        //            }
        //        }

        Person3 person = new Person3();
        person.field("firstName")
              .like("te%");

        Todo3 todo = new Todo3();
        todo.field("todo")
            .is("to-do-1");

        person.getTodos().add(todo);

        List<Person3> personCandidates = db.findAllBy(person);

        /*
            TODO: DbTest: test for join query
         */

        assertNotNull(personCandidates);
        assertEquals("the personCandidates list should contain persons: ", 3, personCandidates.size());
        assertPerson3EqualsAtIndex(personCandidates, 0, "test1", "value2");
        assertPerson3EqualsAtIndex(personCandidates, 1, "testa", "value3");
        assertPerson3EqualsAtIndex(personCandidates, 2, "test222", "value4");

    }

    @Test
    public void testFindBy() throws SQLException, DBValidityException, ResultSetMapperException {

        DB db = new DB(connection);

        try (Statement statement = connection.createStatement()) {

            String insertIntoSql = "INSERT INTO persons2 (firstName, lastName) VALUES ('test1','value2');";
            if (statement.executeUpdate(insertIntoSql) == 0) {
                fail("insert into should not fail.");
            }
        }

        Person2 person = new Person2();
        person.field("firstName")
              .like("te%");

        Person2 personCandidate = db.findBy(person);

        assertNotNull(personCandidate);
        assertEquals("the field firstName should equal to: ", "test1", personCandidate.getFirstName());
        assertEquals("the field lastName should equal to: ", "value2", personCandidate.getLastName());
    }

    @Test
    public void testFindById() throws SQLException, DBValidityException, ResultSetMapperException {

        DB db = new DB(connection);

        Person2 person2 = new Person2();
        person2.setId(1);
        Person2 fetchedPerson1 = db.findById(person2);

        assertNotNull(fetchedPerson1);
        assertEquals("id should be: ", 1, fetchedPerson1.getId());
        assertEquals("first name should be: ", "first111", fetchedPerson1.getFirstName());
        assertEquals("last name should be: ", "last222", fetchedPerson1.getLastName());
        assertEquals("fetched person should have an empty todos list: ", Collections.emptyList(), fetchedPerson1.getTodos());

        Person2 person3 = new Person2(); // no id provided -> should result in empty object
        Person2 fetchedPerson2 = db.findById(person3);

        assertNotNull(fetchedPerson2);
        assertEquals("id should be: ", 0, fetchedPerson2.getId());
        assertEquals("first name should be: ", "", fetchedPerson2.getFirstName());
        assertEquals("last name should be: ", "", fetchedPerson2.getLastName());
        assertEquals("fetched person should have an empty todos list: ", Collections.emptyList(), fetchedPerson2.getTodos());
    }

    @Test
    public void testMultipleIdSave() throws SQLException, DBValidityException, ResultSetMapperException {

        DB db = new DB(connection);

        String name = "first1 last2";
        String email = "email@comp.com";
        String phone = "111-1111-11111";

        Contact4 contact4testData = new Contact4(0L, 1, name, email, phone);

        db.createTable(contact4testData);
        db.save(contact4testData);

        assertTrue("contact4testData should have id over 0: ", contact4testData.getId() > 0);
        assertEquals("contact4testData should have id: ", 1L, contact4testData.getId2());

        Contact4 contact4constraints = new Contact4();
        // name, email, phone
        // "first1 last2", "email@comp.com", "111-1111-11111"
        contact4constraints.field("name").is("first1 last2")
                           .field("email").is("email@comp.com")
                           .field("phone").is("111-1111-11111");

        assertFalse(contact4constraints.isFetched());
        Contact4 contact4Candidate = db.findBy(contact4constraints);

        assertNotNull(contact4Candidate);
        assertTrue("contact4Candidate should return true for isFetched after findBy: ", contact4Candidate.isFetched());
        assertTrue("contact4Candidate should have id over 0: ", contact4Candidate.getId() > 0);
        assertEquals("contact4Candidate name should be: ", name, contact4Candidate.getName());
        assertEquals("contact4Candidate email should be: ", email, contact4Candidate.getEmail());
        assertEquals("contact4Candidate phone should be: ", phone, contact4Candidate.getPhone());

    }

    // mvn -DfailIfNoTests=false -Dtest=DbTest#testRefer test
    @Test
    public void testRefer() throws SQLException, DBValidityException {

        Person person = new Person(1, "first1", "last2", emptyList());
        Todo todo = new Todo("to-do", emptyList());

        SqlBuilder fromSqlBuilder = SqlBuilder.of(person);
        SqlBuilder toSqlBuilder = SqlBuilder.of(todo);

        DB db = new DB(connection);
        db.refer(fromSqlBuilder, toSqlBuilder);

        assertNotNull(person);
        assertNotNull(todo);
        assertEquals("todo should containt the id referring to persons.id: ", person.getId(), todo.getPersonsId());
    }

    private void assertPerson2EqualsAtIndex(List<Person2> personCandidates, int index, String firstName, String lastName) {
        assertEquals("the field firstName should equal to: ", firstName, personCandidates.get(index).getFirstName());
        assertEquals("the field lastName should equal to: ", lastName, personCandidates.get(index).getLastName());
    }

    private void assertPerson3EqualsAtIndex(List<Person3> personCandidates, int index, String firstName, String lastName) {
        assertEquals("the field firstName should equal to: ", firstName, personCandidates.get(index).getFirstName());
        assertEquals("the field lastName should equal to: ", lastName, personCandidates.get(index).getLastName());
    }

    private void assertThatAtLeastOneRowExists(Statement statement, String query, String tableName) throws SQLException {
        logger.debug("SQL:: " + query);
        ResultSet resultSet = statement.executeQuery(query);
        if (!resultSet.next()) {
            fail("database should contain a row for " + tableName);
        }
    }

}
