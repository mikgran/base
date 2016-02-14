package mg.util.db.persist;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static mg.util.Common.flattenToStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.db.persist.field.IdBuilder;
import mg.util.db.persist.field.OneToManyBuilder;
import mg.util.db.persist.field.VarCharBuilder;
import mg.util.db.persist.support.Address;
import mg.util.db.persist.support.Contact;
import mg.util.db.persist.support.Location4;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Person3;
import mg.util.db.persist.support.Person4;
import mg.util.db.persist.support.Todo;
import mg.util.db.persist.support.Todo3;

public class SqlBuilderTest {

    private static Contact contact;
    private static Person person;
    private static Todo todo;

    @BeforeClass
    public static void setupOnce() {
        contact = new Contact(1, "name1", "name1@mail.com", "(111) 111-1111");
        person = new Person(1, "firstName1", "lastName2",
                            asList(new Todo("1st", emptyList()),
                                   new Todo("2nd", emptyList())));
        todo = new Todo("to-do1", emptyList());
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // mvn -DfailIfNoTests=false -Dtest=SqlBuilderTest#testBuildCreateSql test
    @Test
    public void testBuildCreateSql() {

        try {
            // TOIMPROVE: consider cascading creation of tables. Set<String, String> createTables = sqlBuilder.buildCreateTables();

            String expectedPersonsCreateSql = "CREATE TABLE IF NOT EXISTS persons " +
                                              "(firstName VARCHAR(40) NOT NULL, " +
                                              "id BIGINT NOT NULL AUTO_INCREMENT, " +
                                              "lastName VARCHAR(40) NOT NULL, " +
                                              "PRIMARY KEY (id)" +
                                              ");";

            String expectedTodosCreateSql = "CREATE TABLE IF NOT EXISTS todos " +
                                            "(id BIGINT NOT NULL AUTO_INCREMENT, " +
                                            "personsId BIGINT NOT NULL, " +
                                            "todo VARCHAR(40) NOT NULL, " +
                                            "PRIMARY KEY (id), " +
                                            "FOREIGN KEY (personsId) REFERENCES persons(id)" +
                                            ");";

            String builtCreatePersonsSql = SqlBuilder.of(person)
                                                     .buildCreateTable();

            String builtCreateTodosSql = SqlBuilder.of(todo)
                                                   .buildCreateTable();

            assertNotNull(builtCreatePersonsSql);
            assertEquals("create table should equal to: ", expectedPersonsCreateSql, builtCreatePersonsSql);

            assertNotNull(builtCreateTodosSql);
            assertEquals("create table should equal to: ", expectedTodosCreateSql, builtCreateTodosSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }
    }

    @Test
    public void testBuildDelete() {

        try {
            String expectedDeleteSql = "DELETE FROM contacts WHERE id = 1;";

            SqlBuilder SqlBuilder = new SqlBuilder(contact);

            String builtDeleteSql = SqlBuilder.buildDelete();

            assertNotNull(builtDeleteSql);
            assertEquals("delete from should equal to: ", expectedDeleteSql, builtDeleteSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }
    }

    @Test
    public void testBuildDropSql() {

        try {
            SqlBuilder sqlBuilder = new SqlBuilder(contact);

            String builtDropSql = sqlBuilder.buildDropTable();

            assertNotNull(builtDropSql);
            assertEquals("drop table should equal to: ", "DROP TABLE IF EXISTS contacts;", builtDropSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testBuildInsert() {

        try {
            String expectedInsertSql = "INSERT INTO contacts (email, name, phone) VALUES(?, ?, ?);";

            SqlBuilder SqlBuilder = new SqlBuilder(contact);

            String builtInsertSql = SqlBuilder.buildInsert();

            assertNotNull(builtInsertSql);
            assertEquals("insert into should equal to: ", expectedInsertSql, builtInsertSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }
    }

    @Test
    public void testBuildSelectByFields() {

        try {
            {
                String expectedSelectByFields = "SELECT c1.email, c1.id, c1.name, c1.phone " +
                                                "FROM contacts AS c1 " +
                                                "WHERE c1.name = 'first1 last2' " +
                                                "AND c1.email LIKE 'first1%';";

                Contact contact = new Contact();
                contact.field("name").is("first1 last2")
                       .field("email").like("first1%");

                SqlBuilder sqlBuilder = SqlBuilder.of(contact);

                String builtSelectByFields = sqlBuilder.buildSelectByFields();

                assertNotNull(builtSelectByFields);
                assertEquals("select by should equal to: ", expectedSelectByFields, builtSelectByFields);

                String expectedSelectByFields2 = "SELECT c1.email, c1.id, c1.name, c1.phone " +
                                                 "FROM contacts AS c1 " +
                                                 "WHERE c1.name = 'first1 last2' " +
                                                 "AND c1.email LIKE 'first1%' " +
                                                 "AND c1.phone = '(111) 111-1111';";

                contact.field("phone").is("(111) 111-1111");

                String builtSelectByFields2 = sqlBuilder.buildSelectByFields();

                assertNotNull(builtSelectByFields2);
                assertEquals("select by should equal to: ", expectedSelectByFields2, builtSelectByFields2);
                assertEquals("sqlBuilder should have constraints: ", 3, sqlBuilder.getConstraints().size());
                assertEquals("sqlBuilder should have constraints: ", 3, contact.getConstraints().size());

                contact.clearConstraints();

                assertEquals("sqlBuilder should have constraints: ", 0, sqlBuilder.getConstraints().size());
                assertEquals("persistable should have constraints: ", 0, contact.getConstraints().size());
            }
            {
                String expectedSelectByFields = "SELECT c1.email, c1.id, c1.name, c1.phone " +
                                                "FROM contacts AS c1 " +
                                                "WHERE c1.name = 'testName1 testSurname2';";

                Contact contact = new Contact();
                contact.field("name").is("testName1 testSurname2");

                SqlBuilder sqlBuilder = SqlBuilder.of(contact);

                String builtSelectByFields = sqlBuilder.buildSelectByFields();

                assertNotNull(builtSelectByFields);
                assertEquals("select by should equal to: ", expectedSelectByFields, builtSelectByFields);
                assertEquals("sqlBuilder should have constraints: ", 1, sqlBuilder.getConstraints().size());
                assertEquals("contact should have constraints: ", 1, contact.getConstraints().size());
            }

        } catch (DBValidityException e) {

            fail("SqlBuilder.of(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testBuildSelectByFieldsJoin() throws DBValidityException {

        try {
            Person3 person3 = new Person3();
            person3.field("firstName").is("first1");

            Todo3 todo3 = new Todo3();
            todo3.field("todo").is("a-to-do");

            person3.getTodos().add(todo3);

            String expectedSelectByFields = "SELECT p1.firstName, p1.id, p1.lastName, t1.id, t1.personsId, t1.todo " +
                                            "FROM persons3 AS p1 " +
                                            "LEFT JOIN todos3 AS t1 " +
                                            "ON p1.id = t1.personsId " +
                                            "WHERE " +
                                            "p1.firstName = 'first1' AND " +
                                            "t1.todo = 'a-to-do';";

            SqlBuilder sqlBuilder = SqlBuilder.of(person3);

            String builtSelectByFields = sqlBuilder.buildSelectByFields();

            assertNotNull(builtSelectByFields);
            assertEquals("select by should equal to: ", expectedSelectByFields, builtSelectByFields);
            assertEquals("sqlBuilder should have constraints: ", 1, sqlBuilder.getConstraints().size());
            assertEquals("person3 should have constraints: ", 1, person3.getConstraints().size());
            assertEquals("todo3 should have constraints: ", 1, todo3.getConstraints().size());

        } catch (DBValidityException e) {

            fail("SqlBuilder.of(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testBuildUpdate() {

        try {
            String expectedUpdateSql = "UPDATE contacts SET email = ?, name = ?, phone = ?;";

            SqlBuilder sqlBuilder = SqlBuilder.of(contact);

            String builtUpdateSql = sqlBuilder.buildUpdate();

            assertNotNull(builtUpdateSql);
            assertEquals("update should equal to: ", expectedUpdateSql, builtUpdateSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder.of(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }
    }

    @Test
    public void testConstruction() {

        try {

            SqlBuilder sqlBuilder = SqlBuilder.of(contact);

            assertEquals("SqlBuilder should have the table name for Type Contact: ", "contacts", sqlBuilder.getTableName());

            List<FieldBuilder> fieldBuilders = sqlBuilder.getFieldBuilders();
            List<FieldBuilder> collectionBuilders = sqlBuilder.getOneToManyBuilders();

            assertNotNull(fieldBuilders);
            assertNotNull(collectionBuilders);
            assertEquals("fieldBuilders should have elements: ", 4, fieldBuilders.size());
            assertEquals("collectionBuilders should have no elements: ", 0, collectionBuilders.size());

            assertFieldEquals("email", "name1@mail.com", "email VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(0), contact);
            assertFieldEquals("id", 1L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuilders.get(1), contact);
            assertFieldEquals("name", "name1", "name VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(2), contact);
            assertFieldEquals("phone", "(111) 111-1111", "phone VARCHAR(20) NOT NULL", VarCharBuilder.class, fieldBuilders.get(3), contact);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

        try {

            SqlBuilder sqlBuilder = SqlBuilder.of(person);

            assertEquals("SqlBuilder should have the table name for Type Contact: ", "persons", sqlBuilder.getTableName());

            List<FieldBuilder> fieldBuilders = sqlBuilder.getFieldBuilders();
            List<FieldBuilder> collectionBuilders = sqlBuilder.getOneToManyBuilders();

            assertNotNull(fieldBuilders);
            assertNotNull(collectionBuilders);
            assertEquals("fieldBuilders should have elements: ", 3, fieldBuilders.size());
            assertEquals("collectionBuilders should have elements: ", 1, collectionBuilders.size());
            assertEquals("collectionBuilders element 1 should have the size of: ", 2, (((Collection<?>) collectionBuilders.get(0).getFieldValue(person)).size()));

            assertFieldEquals("firstName", "firstName1", "firstName VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(0), person);
            assertFieldEquals("id", 1L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuilders.get(1), person);
            assertFieldEquals("lastName", "lastName2", "lastName VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(2), person);
            assertCollectionFieldEquals("todos", "[Todo('0', '0', '1st'), Todo('0', '0', '2nd')]", "[N/A]", OneToManyBuilder.class,
                                        collectionBuilders.get(0), person);

            List<Persistable> persistables;
            persistables = sqlBuilder.getOneToManyBuilders()
                                     .stream()
                                     .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getFieldValue(person)))
                                     .map(object -> (Persistable) object)
                                     .collect(Collectors.toList());

            assertNotNull(persistables);
            assertEquals("there should be 2 Todos in Person: ", 2, persistables.size());

            Persistable persistable = persistables.get(0);
            SqlBuilder sqlBuilderTodo1 = new SqlBuilder(persistable);
            List<FieldBuilder> fieldBuildersTodo1 = sqlBuilderTodo1.getFieldBuilders();
            List<FieldBuilder> collectionBuildersTodo1 = sqlBuilderTodo1.getOneToManyBuilders();

            assertNotNull(fieldBuildersTodo1);
            assertNotNull(collectionBuildersTodo1);
            assertEquals("fieldBuildersTodo1 should have elements: ", 3, fieldBuildersTodo1.size());
            assertEquals("collectionBuildersTodo1 should have elements: ", 1, collectionBuildersTodo1.size());
            assertEquals("collectionBuildersTodo1s element 1 should have no elements: ", 0,
                         (((Collection<?>) collectionBuildersTodo1.get(0).getFieldValue(persistables.get(0))).size()));

            assertFieldEquals("id", 0L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuildersTodo1.get(0), persistables.get(0));
            assertFieldEquals("personsId", 0L, "personsId BIGINT NOT NULL", ForeignKeyBuilder.class, fieldBuildersTodo1.get(1), persistables.get(0));
            assertFieldEquals("todo", "1st", "todo VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuildersTodo1.get(2), persistables.get(0));

            SqlBuilder sqlBuilderTodo2 = new SqlBuilder(persistables.get(1));
            List<FieldBuilder> fieldBuildersTodo2 = sqlBuilderTodo2.getFieldBuilders();
            List<FieldBuilder> collectionBuildersTodo2 = sqlBuilderTodo2.getOneToManyBuilders();

            assertNotNull(fieldBuildersTodo2);
            assertNotNull(collectionBuildersTodo2);
            assertEquals("fieldBuildersTodo2 should have elements: ", 3, fieldBuildersTodo2.size());
            assertEquals("collectionBuildersTodo2 should have elements: ", 1, collectionBuildersTodo2.size());
            assertEquals("collectionBuildersTodo2s element 1 should have no elements: ", 0,
                         (((Collection<?>) collectionBuildersTodo2.get(0).getFieldValue(persistables.get(1))).size()));

            assertFieldEquals("id", 0L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuildersTodo2.get(0), persistables.get(1));
            assertFieldEquals("personsId", 0L, "personsId BIGINT NOT NULL", ForeignKeyBuilder.class, fieldBuildersTodo2.get(1), persistables.get(1));
            assertFieldEquals("todo", "2nd", "todo VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuildersTodo2.get(2), persistables.get(1));

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testGetReferenceBuildersByClassCascading1level() throws DBValidityException {

        Person4 person4 = new Person4();
        Location4 location4 = new Location4();
        person4.getLocations().add(location4);

        SqlBuilder person4Builder = SqlBuilder.of(person4);

        Map<Persistable, List<Persistable>> refsByPersistable = person4Builder.getReferencePersistablesByRootCascading(person4);

        assertNotNull(refsByPersistable);
        List<Persistable> list = refsByPersistable.get(person4);
        assertEquals("there should be Persistable for Person4: ", 2, list.size());
        List<Persistable> list2 = refsByPersistable.get(location4);
        assertEquals("there should be no Persistables for Location4: ", 0, list2.size());
        List<Persistable> list3 = refsByPersistable.get(person4.getAddress());
        assertEquals("there should be no Persistables for Address: ", 0, list3.size());

        assertTrue("there should be an Address Persistable in list by person4: ",
                   list.stream()
                       .anyMatch(s -> Address.class.equals(s.getClass())));

        assertTrue("there should be an Locations4 Persistable in list by person4: ",
                   list.stream()
                       .anyMatch(s -> Location4.class.equals(s.getClass())));
    }

    @Test
    public void testSelectById() {

        try {
            String expectedSelectByIdSql = "SELECT c1.email, c1.id, c1.name, c1.phone FROM contacts AS c1 WHERE c1.id = 1;";

            SqlBuilder sqlBuilder = SqlBuilder.of(contact);

            String builtSelectById = sqlBuilder.buildSelectByIds();

            assertNotNull(builtSelectById);
            assertEquals("select by should equal to: ", expectedSelectByIdSql, builtSelectById);

        } catch (DBValidityException e) {

            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testSelectByIdLazy() throws Exception {

        Person4 personLazy = new Person4(new Address("address"), "firstName1", "lastName2", asList(new Location4("1st loc")));
        personLazy.setId(1);
        personLazy.field("firstName").is("first1");

        SqlBuilder sqlBuilder = SqlBuilder.of(personLazy);

        String expectedSelectByIds = "SELECT p1.firstName, p1.id, p1.lastName " +
                                     "FROM persons4 AS p1 " +
                                     "WHERE " +
                                     "p1.id = 1 AND " +
                                     "p1.firstName = 'first1';";

        String builtSelectByIdsLazy = sqlBuilder.buildSelectByIdsLazy();
        assertNotNull(builtSelectByIdsLazy);
        assertEquals("the lazy building should produce only root level SELECT clause: ", expectedSelectByIds, builtSelectByIdsLazy);
    }

    @Test
    public void testSelectByIdLazyCaseRefs() throws Exception {

        Person4 personLazy = new Person4(new Address("address"), "firstName1", "lastName2", asList(new Location4("1st loc")));
        personLazy.setId(5);

        // case address
        {
            SqlBuilder personBuilder = SqlBuilder.of(personLazy);
            Address address = personLazy.getAddress();
            address.field("address").is("street1");
            SqlBuilder addressBuilder = SqlBuilder.of(address);

            String expectedSelectByIds = "SELECT a1.address, a1.id, a1.personsId " +
                                         "FROM addresses AS a1 " +
                                         "WHERE " +
                                         "a1.personsId = 5 " +
                                         "AND a1.address = 'street1';";

            String builtSelectByIdsLazy = personBuilder.buildSelectByRefIds(addressBuilder);
            assertNotNull(builtSelectByIdsLazy);
            assertEquals("the lazy building should produce only root level SELECT clause: ", expectedSelectByIds, builtSelectByIdsLazy);
        }
        // case locations (Collection)
        {
            SqlBuilder personBuilder = SqlBuilder.of(personLazy);
            personLazy.setId(8);
            String expectedSelectByIds = "SELECT l1.id, l1.location, l1.personsId " +
                                         "FROM locations4 AS l1 " +
                                         "WHERE " +
                                         "l1.personsId = 8;";

            Location4 location4 = personLazy.getLocations().get(0);
            SqlBuilder locationBuilder = SqlBuilder.of(location4);

            String builtSelectByRefIdsLazy = personBuilder.buildSelectByRefIds(locationBuilder);
            assertNotNull(builtSelectByRefIdsLazy);
            assertEquals("the lazy building should produce only root level SELECT clause: ", expectedSelectByIds, builtSelectByRefIdsLazy);
        }
    }

    private void assertCollectionFieldEquals(String fieldName, String fieldValue, String sql, Class<?> expectedClass, FieldBuilder fieldBuilder, Persistable type) {
        assertEquals("class should be: ", expectedClass, fieldBuilder.getClass());
        assertEquals(format("fieldBuilder %s: name: ", fieldName), fieldName, fieldBuilder.getName());
        assertEquals(format("fieldBuilder %s: value: ", fieldName), fieldValue, fieldBuilder.getFieldValue(type).toString());
        assertEquals(format("fieldBuilder %s: sql: ", fieldName), sql, fieldBuilder.build());
    }
    private void assertFieldEquals(String fieldName, Object fieldValue, String sql, Class<?> expectedClass, FieldBuilder fieldBuilder, Persistable type) {

        assertEquals("class should be: ", expectedClass, fieldBuilder.getClass());
        assertEquals(format("fieldBuilder %s: name: ", fieldName), fieldName, fieldBuilder.getName());
        assertEquals(format("fieldBuilder %s: value: ", fieldName), fieldValue, fieldBuilder.getFieldValue(type));
        assertEquals(format("fieldBuilder %s: sql: ", fieldName), sql, fieldBuilder.build());
    }

}
