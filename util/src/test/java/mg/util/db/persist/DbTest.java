package mg.util.db.persist;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.support.Address;
import mg.util.db.persist.support.Address2;
import mg.util.db.persist.support.Contact;
import mg.util.db.persist.support.Contact2;
import mg.util.db.persist.support.Contact3;
import mg.util.db.persist.support.Contact4;
import mg.util.db.persist.support.Contact6;
import mg.util.db.persist.support.Location;
import mg.util.db.persist.support.Location3;
import mg.util.db.persist.support.Location4;
import mg.util.db.persist.support.Location5;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Person2;
import mg.util.db.persist.support.Person3;
import mg.util.db.persist.support.Person4;
import mg.util.db.persist.support.Person5;
import mg.util.db.persist.support.Todo;
import mg.util.db.persist.support.Todo2;
import mg.util.db.persist.support.Todo3;
import mg.util.functional.consumer.ThrowingConsumer;

public class DbTest {

    private static Connection connection;
    private static List<Persistable> testValues;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");

        DB db = new DB(connection);

        Person2 person2 = new Person2("first111", "last222");
        Person2 person2b = new Person2("test1", "value2");
        Person2 person2c = new Person2("testa", "value3");
        Person2 person2d = new Person2("test222", "value4");

        testValues = asList(new Address(),
                            new Address2(),
                            new Location3(),
                            new Location4(),
                            new Location5(),
                            new Todo(),
                            new Todo2(),
                            new Todo3(),
                            new Person(),
                            person2,
                            person2b,
                            person2c,
                            person2d,
                            new Person3(),
                            new Person4(),
                            new Person5());

        dropAndCreateTables(db, testValues);

        db.save(person2);
        db.save(person2b);
        db.save(person2c);
        db.save(person2d);
    }

    @AfterClass
    public static void tearDownOnce() throws Exception {
        DB db = new DB(connection);

        dropTables(db, testValues);

        // dropped separately
        dropTables(db, asList(new Location(),
                              new Contact(),
                              new Contact2(),
                              new Contact3(),
                              new Contact4()));

        Common.close(connection);
    }

    private static void dropAndCreateTables(DB db, List<Persistable> persistables) throws Exception {

        dropTables(db, persistables);

        persistables.stream()
                    .collect(Collectors.toCollection(LinkedList::new))
                    .descendingIterator()
                    .forEachRemaining((ThrowingConsumer<Persistable, Exception>) persistable -> {

                        db.createTable(persistable);
                    });
    }

    private static void dropTables(DB db, List<Persistable> persistables) throws Exception {

        persistables.forEach((ThrowingConsumer<Persistable, Exception>) p -> {
            db.dropTable(p);
        });
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
    public void testDBO() throws Exception {

        // these are just delegate method testers: not null && right amount of candidates.
        Contact6 contact6 = new Contact6(connection, 0L, "test testey", "test@mail.com", "123");
        Contact6 contact6b = new Contact6(connection, 0L, "john doe", "john@mail.com", "456");

        contact6.dropTable();
        contact6.createTable();
        contact6.save();
        contact6b.save();

        List<Contact6> candidate = contact6.findAll();

        assertNotNull(candidate);
        assertEquals("size of list: ", 2, candidate.size());

        // TOIMPROVE: currently the sql fetch only maps via properly aliased queries -> allow partial mapping
        candidate = contact6.findAllBy("SELECT c1.email, c1.id, c1.name, c1.phone FROM contacts6 c1");
        assertNotNull(candidate);
        assertEquals("size of list: ", 2, candidate.size());

        contact6.setId(2L);
        Contact6 candidateContact6 = contact6.findById();
        assertNotNull(candidateContact6);
    }

    @Test
    public void testFindAllBy() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        Person2 person = new Person2();
        person.field("firstName").like("te%");

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

        // XXX test ordering
    }

    @Test
    public void testFindAllByJoin() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        List<Person3> testValues = asList(new Person3("test1", "value2", asList(new Todo3("to-do-1", asList(new Location3("a location1"))),
                                                                                new Todo3("to-do-2", asList(new Location3("a location2"),
                                                                                                            new Location3("a location3"))))),
                                          new Person3("testa", "value3"),
                                          new Person3("test222", "value4", asList(new Todo3("to-do-3", asList(new Location3("a location4"))))));

        testValues.stream()
                  .forEach((ThrowingConsumer<Persistable, Exception>) p -> db.save(p));

        Person3 person = new Person3();
        person.field("firstName").like("te%");

        Todo3 todo = new Todo3();

        Location3 location = new Location3();

        todo.getLocations().add(location);
        person.getTodos().add(todo);

        List<Person3> personCandidates = db.findAllBy(person);

        assertNotNull(personCandidates);
        assertEquals("the personCandidates list should contain persons: ", 3, personCandidates.size());
        assertPerson3EqualsAtIndex(personCandidates, 0, "test1", "value2");

        {
            // person at index 0
            Person3 person3At0 = personCandidates.get(0);
            List<Todo3> todosOfPerson3At0 = person3At0.getTodos();
            assertEquals("person3At0Todos size should be: ", 2, todosOfPerson3At0.size());
            Todo3 todoAt0Ofperson3At0 = todosOfPerson3At0.get(0);
            String expectedTodoAt0Ofperson3At0ToString = "Todo3('1', '1', 'to-do-1', [Location3('1', 'a location1', '1')])";
            assertEquals("person3At0TodosAt0 toString should equal to: ",
                         expectedTodoAt0Ofperson3At0ToString,
                         todoAt0Ofperson3At0.toString());
            Todo3 person3At0TodosAt1 = todosOfPerson3At0.get(1);
            String expectedTodoAt1Ofperson3At0ToString = "Todo3('2', '1', 'to-do-2', [" +
                                                         "Location3('2', 'a location2', '2'), " +
                                                         "Location3('3', 'a location3', '2')])";
            assertEquals("person3At0TodosAt0 toString should equal to: ",
                         expectedTodoAt1Ofperson3At0ToString,
                         person3At0TodosAt1.toString());
        }
        {
            // person at index 1
            assertPerson3EqualsAtIndex(personCandidates, 1, "testa", "value3");
            Person3 person3At1 = personCandidates.get(1);
            List<Todo3> todosOfPerson3At1 = person3At1.getTodos();
            assertEquals("person3At0Todos size should be: ", 0, todosOfPerson3At1.size());
        }
        {
            // person at index 2
            assertPerson3EqualsAtIndex(personCandidates, 2, "test222", "value4");
            Person3 person3At2 = personCandidates.get(2);
            List<Todo3> todosOfPerson3At2 = person3At2.getTodos();
            assertEquals("person3At0Todos size should be: ", 1, todosOfPerson3At2.size());
            Todo3 todoAt0Ofperson3At2 = todosOfPerson3At2.get(0);
            String expectedTodoAt0Ofperson3At2ToString = "Todo3('3', '3', 'to-do-3', [Location3('4', 'a location4', '3')])";
            assertEquals("person3At0TodosAt0 toString should equal to: ",
                         expectedTodoAt0Ofperson3At2ToString,
                         todoAt0Ofperson3At2.toString());
        }
    }

    @Test
    public void testFindAllByJoinNullsInOneToAny() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        ThrowingConsumer<Person5, Exception> savePerson5 = p -> db.save(p);

        Person5 person5a = new Person5(new Address2("Street 5 A 5 00111 City"),
                                       "test1", "value1",
                                       null);

        Person5 person5b = new Person5(null,
                                       "test2", "value2",
                                       asList(new Location5("loc3")));

        asList(person5a, person5b).forEach(savePerson5);

        {
            // case address == null, locations == null
            Person5 person = new Person5();
            person.field("firstName").like("test%");
            List<Person5> personCandidates = db.findAllBy(person);

            assertNotNull(personCandidates);
            assertEquals("the personCandidates list should contain persons: ", 2, personCandidates.size());
            assertPerson5EqualsAtIndex(personCandidates, 0, "test1", "value1");
            assertPerson5EqualsAtIndex(personCandidates, 1, "test2", "value2");

            Person5 person5At0 = personCandidates.get(0);
            Address2 person5At0Address = person5At0.getAddress();
            assertNull(person5At0Address);
            assertEquals("there should be 0 locations for person5At0, when finding with null address and null locations: ", 0, person5At0.getLocations().size());
        }
        {
            // case address != null, locations == null
            Person5 person = new Person5(new Address2(), "", "", null);
            person.field("firstName").like("test%");
            List<Person5> personCandidates = db.findAllBy(person);

            assertNotNull(personCandidates);
            assertEquals("the personCandidates list should contain persons: ", 2, personCandidates.size());
            assertPerson5EqualsAtIndex(personCandidates, 0, "test1", "value1");

            Person5 person5At0 = personCandidates.get(0);
            Address2 person5At0Address = person5At0.getAddress();
            assertNotNull(person5At0Address);
            assertEquals("the person5At0Address of person5At0 should equal to: ", "Street 5 A 5 00111 City", person5At0Address.getAddress());
            assertEquals("the id of person5At0 should equal to: ", 1L, person5At0Address.getId());
            assertEquals("the personsId of person5At0 should equal to: ", 1L, person5At0Address.getPersonsId());
            assertEquals("there should be 0 locations for person5At0, when finding with not null address and null locations: ", 0, person5At0.getLocations().size());
        }
        {
            // case address == null, locations != null
            Person5 person = new Person5(null, "", "", asList(new Location5()));
            person.field("firstName").like("test%");
            List<Person5> personCandidates = db.findAllBy(person);

            assertNotNull(personCandidates);
            assertEquals("the personCandidates list should contain persons: ", 2, personCandidates.size());
            assertPerson5EqualsAtIndex(personCandidates, 1, "test2", "value2");

            Person5 person5At0 = personCandidates.get(1);
            Address2 person5At0Address = person5At0.getAddress();
            assertNull(person5At0Address);
            assertEquals("there should be 0 locations for person5At0, when finding with null address and not null locations: ",
                         1,
                         person5At0.getLocations().size());

            assertEquals("there should be one location5 for person5At0: ", "Location5('1', 'loc3', '2')", person5At0.getLocations().get(0).toString());
        }
        {
            // TOIMPROVE: testcoverage: join policy: JOIN, LEFT JOIN (assumed to produce missing fields as nulls: include null handling)
            // case address != null, locations != null
            Person5 person = new Person5(new Address2(), "", "", asList(new Location5()));
            person.field("firstName").like("test%");
            List<Person5> personCandidates = db.findAllBy(person);

            assertNotNull(personCandidates);
            assertEquals("the personCandidates list should contain persons: ", 2, personCandidates.size());
        }

    }

    @Test
    public void testFindAllByJoinOneToOneCase() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        List<Person4> testValues = asList(new Person4(new Address("Street 1 A 1 00111 City"), "test1", "value2", asList(new Location4("a loc"))));
        testValues.forEach((ThrowingConsumer<Person4, Exception>) p -> db.save(p));

        Person4 person = new Person4();
        person.field("firstName").is("test1");

        person.getAddress()
              .field("address").like("Street 1%");

        person.getLocations().add(new Location4());

        List<Person4> personCandidates = db.findAllBy(person);

        assertNotNull(personCandidates);
        assertEquals("the personCandidates list should contain persons: ", 1, personCandidates.size());
        assertPerson4EqualsAtIndex(personCandidates, 0, "test1", "value2");

        Person4 person4At0 = personCandidates.get(0);
        Address person4At0Address = person4At0.getAddress();

        assertNotNull(person4At0Address);
        assertEquals("the address of person4At0 should equal to: ", "Street 1 A 1 00111 City", person4At0Address.getAddress());
        assertEquals("the id of person4At0 should equal to: ", 1L, person4At0Address.getId());
        assertEquals("the personsId of person4At0 should equal to: ", 1L, person4At0Address.getPersonsId());
    }

    @Test
    public void testFindBy() throws SQLException, DBValidityException, DBMappingException {

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
    public void testFindByIds() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        Person2 person2 = new Person2();
        person2.setId(1);
        Person2 fetchedPerson1 = db.findById(person2);

        assertNotNull(fetchedPerson1);
        assertEquals("id should be: ", 1, fetchedPerson1.getId());
        assertEquals("first name should be: ", "first111", fetchedPerson1.getFirstName());
        assertEquals("last name should be: ", "last222", fetchedPerson1.getLastName());
        assertEquals("fetched person should have an empty todos list: ", Collections.emptyList(), fetchedPerson1.getTodos());

        Person2 person3 = new Person2(); // no id provided -> null;
        Person2 fetchedPerson2 = db.findById(person3);

        assertNull(fetchedPerson2);
    }

    @Test
    public void testMultipleIdSave() throws SQLException, DBValidityException, DBMappingException {

        DB db = new DB(connection);

        String name = "first1 last2";
        String email = "email@comp.com";
        String phone = "111-1111-11111";

        Contact4 contact4testData = new Contact4(1L, 0L, name, email, phone);

        db.createTable(contact4testData);
        db.save(contact4testData);

        assertTrue("contact4testData should have id over 0: ", contact4testData.getId2() > 0);
        assertEquals("contact4testData should have id2: ", 1L, contact4testData.getId());

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
        assertTrue("contact4Candidate should have id over 0: ", contact4Candidate.getId() > 0); // any other contact4 save will
        assertEquals("contact4Candidate id2 should be: ", 1, contact4Candidate.getId2());
        assertEquals("contact4Candidate name should be: ", name, contact4Candidate.getName());
        assertEquals("contact4Candidate email should be: ", email, contact4Candidate.getEmail());
        assertEquals("contact4Candidate phone should be: ", phone, contact4Candidate.getPhone());

    }

    // mvn -DfailIfNoTests=false -Dtest=DbTest#testRefer test
    @Test
    public void testRefer() throws SQLException, DBValidityException {

        Person person = new Person(1, "first1", "last2", emptyList());
        Todo todo = new Todo("to-do", emptyList());

        SqlBuilder fromSqlBuilder = SqlBuilderFactory.of(person);
        SqlBuilder toSqlBuilder = SqlBuilderFactory.of(todo);

        DB db = new DB(connection);
        db.refer(fromSqlBuilder, toSqlBuilder);

        assertNotNull(person);
        assertNotNull(todo);
        assertEquals("todo should containt the id referring to persons.id: ", person.getId(), todo.getPersonsId());
    }

    @Test
    public void testTableDropsCreates() throws Exception {

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
    public void testTableSavesRemoves() throws Exception {

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

    private void assertPerson2EqualsAtIndex(List<Person2> personCandidates, int index, String firstName, String lastName) {
        assertEquals("the field firstName should equal to: ", firstName, personCandidates.get(index).getFirstName());
        assertEquals("the field lastName should equal to: ", lastName, personCandidates.get(index).getLastName());
    }

    private void assertPerson3EqualsAtIndex(List<Person3> personCandidates, int index, String firstName, String lastName) {
        assertEquals("the field firstName should equal to: ", firstName, personCandidates.get(index).getFirstName());
        assertEquals("the field lastName should equal to: ", lastName, personCandidates.get(index).getLastName());
    }

    private void assertPerson4EqualsAtIndex(List<Person4> personCandidates, int index, String firstName, String lastName) {
        assertEquals("the field firstName should equal to: ", firstName, personCandidates.get(index).getFirstName());
        assertEquals("the field lastName should equal to: ", lastName, personCandidates.get(index).getLastName());
    }

    private void assertPerson5EqualsAtIndex(List<Person5> personCandidates, int index, String firstName, String lastName) {
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
