package mg.util.db.persist;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static mg.util.Common.flattenToStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.persist.field.CollectionBuilder;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.db.persist.field.IdBuilder;
import mg.util.db.persist.field.VarCharBuilder;
import mg.util.db.persist.support.Contact;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Person3;
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
                String expectedSelectByFields = "SELECT * FROM contacts WHERE contacts.name = 'first1 last2' AND contacts.email LIKE 'first1%';";

                Contact contact = new Contact();
                contact.field("name")
                       .is("first1 last2")
                       .field("email")
                       .like("first1%");

                SqlBuilder sqlBuilder = SqlBuilder.of(contact);

                String builtSelectByFields = sqlBuilder.buildSelectByFields();

                assertNotNull(builtSelectByFields);
                assertEquals("select by should equal to: ", expectedSelectByFields, builtSelectByFields);

                String expectedSelectByFields2 = "SELECT * FROM contacts WHERE contacts.name = 'first1 last2' AND contacts.email LIKE 'first1%' AND contacts.phone = '(111) 111-1111';";

                contact.field("phone")
                       .is("(111) 111-1111");

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
                String expectedSelectByFields = "SELECT * FROM contacts WHERE contacts.name = 'testName1 testSurname2';";

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

            String expectedSelectByFields = "SELECT * FROM persons3 " +
                                            "JOIN todos3 " +
                                            "ON persons3.id = todos3.personsId " +
                                            "WHERE " +
                                            "persons3.firstName = 'first1' AND " +
                                            "todos3.todo = 'a-to-do';";

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
            List<FieldBuilder> collectionBuilders = sqlBuilder.getCollectionBuilders();

            assertNotNull(fieldBuilders);
            assertNotNull(collectionBuilders);
            assertEquals("fieldBuilders should have elements: ", 4, fieldBuilders.size());
            assertEquals("collectionBuilders should have no elements: ", 0, collectionBuilders.size());

            assertFieldEquals("email", "name1@mail.com", "email VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(0));
            assertFieldEquals("id", 1L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuilders.get(1));
            assertFieldEquals("name", "name1", "name VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(2));
            assertFieldEquals("phone", "(111) 111-1111", "phone VARCHAR(20) NOT NULL", VarCharBuilder.class, fieldBuilders.get(3));

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

        try {

            SqlBuilder sqlBuilder = SqlBuilder.of(person);

            assertEquals("SqlBuilder should have the table name for Type Contact: ", "persons", sqlBuilder.getTableName());

            List<FieldBuilder> fieldBuilders = sqlBuilder.getFieldBuilders();
            List<FieldBuilder> collectionBuilders = sqlBuilder.getCollectionBuilders();

            assertNotNull(fieldBuilders);
            assertNotNull(collectionBuilders);
            assertEquals("fieldBuilders should have elements: ", 3, fieldBuilders.size());
            assertEquals("collectionBuilders should have elements: ", 1, collectionBuilders.size());
            assertEquals("collectionBuilders element 1 should have the size of: ", 2, (((Collection<?>) collectionBuilders.get(0).getValue()).size()));

            assertFieldEquals("firstName", "firstName1", "firstName VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(0));
            assertFieldEquals("id", 1L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuilders.get(1));
            assertFieldEquals("lastName", "lastName2", "lastName VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(2));
            assertCollectionFieldEquals("todos", "[[id: '0', personsId: '0', todo: '1st'], [id: '0', personsId: '0', todo: '2nd']]", "[N/A]", CollectionBuilder.class,
                                        collectionBuilders.get(0));

            List<Persistable> persistables;
            persistables = sqlBuilder.getCollectionBuilders()
                                     .stream()
                                     .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                                     .map(object -> (Persistable) object)
                                     .collect(Collectors.toList());

            assertNotNull(persistables);
            assertEquals("there should be 2 Todos in Person: ", 2, persistables.size());

            SqlBuilder sqlBuilderTodo1 = new SqlBuilder(persistables.get(0));
            List<FieldBuilder> fieldBuildersTodo1 = sqlBuilderTodo1.getFieldBuilders();
            List<FieldBuilder> collectionBuildersTodo1 = sqlBuilderTodo1.getCollectionBuilders();

            assertNotNull(fieldBuildersTodo1);
            assertNotNull(collectionBuildersTodo1);
            assertEquals("fieldBuildersTodo1 should have elements: ", 3, fieldBuildersTodo1.size());
            assertEquals("collectionBuildersTodo1 should have elements: ", 1, collectionBuildersTodo1.size());
            assertEquals("collectionBuildersTodo1s element 1 should have no elements: ", 0, (((Collection<?>) collectionBuildersTodo1.get(0).getValue()).size()));

            assertFieldEquals("id", 0L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuildersTodo1.get(0));
            assertFieldEquals("personsId", 0L, "personsId BIGINT NOT NULL", ForeignKeyBuilder.class, fieldBuildersTodo1.get(1));
            assertFieldEquals("todo", "1st", "todo VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuildersTodo1.get(2));

            SqlBuilder sqlBuilderTodo2 = new SqlBuilder(persistables.get(1));
            List<FieldBuilder> fieldBuildersTodo2 = sqlBuilderTodo2.getFieldBuilders();
            List<FieldBuilder> collectionBuildersTodo2 = sqlBuilderTodo2.getCollectionBuilders();

            assertNotNull(fieldBuildersTodo2);
            assertNotNull(collectionBuildersTodo2);
            assertEquals("fieldBuildersTodo2 should have elements: ", 3, fieldBuildersTodo2.size());
            assertEquals("collectionBuildersTodo2 should have elements: ", 1, collectionBuildersTodo2.size());
            assertEquals("collectionBuildersTodo2s element 1 should have no elements: ", 0, (((Collection<?>) collectionBuildersTodo2.get(0).getValue()).size()));

            assertFieldEquals("id", 0L, "id BIGINT NOT NULL AUTO_INCREMENT", IdBuilder.class, fieldBuildersTodo2.get(0));
            assertFieldEquals("personsId", 0L, "personsId BIGINT NOT NULL", ForeignKeyBuilder.class, fieldBuildersTodo2.get(1));
            assertFieldEquals("todo", "2nd", "todo VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuildersTodo2.get(2));

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testSelectById() {

        // TODO: testSelectById: alias short-form-generator

        try {
            String expectedSelectByIdSql = "SELECT c1.email, c1.id, c1.name, c1.phone FROM contacts AS c1 WHERE id = 1;";

            SqlBuilder sqlBuilder = SqlBuilder.of(contact);

            String builtSelectById = sqlBuilder.buildSelectByIds();

            assertNotNull(builtSelectById);
            assertEquals("select by should equal to: ", expectedSelectByIdSql, builtSelectById);

        } catch (DBValidityException e) {

            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    private void assertCollectionFieldEquals(String fieldName, String fieldValue, String sql, Class<?> expectedClass, FieldBuilder fieldBuilder) {
        assertEquals("class should be: ", expectedClass, fieldBuilder.getClass());
        assertEquals(format("fieldBuilder %s: name: ", fieldName), fieldName, fieldBuilder.getName());
        assertEquals(format("fieldBuilder %s: value: ", fieldName), fieldValue, fieldBuilder.getValue().toString());
        assertEquals(format("fieldBuilder %s: sql: ", fieldName), sql, fieldBuilder.build());
    }
    private void assertFieldEquals(String fieldName, Object fieldValue, String sql, Class<?> expectedClass, FieldBuilder fieldBuilder) {

        assertEquals("class should be: ", expectedClass, fieldBuilder.getClass());
        assertEquals(format("fieldBuilder %s: name: ", fieldName), fieldName, fieldBuilder.getName());
        assertEquals(format("fieldBuilder %s: value: ", fieldName), fieldValue, fieldBuilder.getValue());
        assertEquals(format("fieldBuilder %s: sql: ", fieldName), sql, fieldBuilder.build());
    }

}
