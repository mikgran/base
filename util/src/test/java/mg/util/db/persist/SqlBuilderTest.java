package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.persist.field.CollectionBuilder;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.VarCharBuilder;
import mg.util.db.persist.support.Contact;
import mg.util.db.persist.support.Person;
import mg.util.db.persist.support.Todo;

public class SqlBuilderTest {

    private static Contact contact;
    private static Person person;

    @BeforeClass
    public static void setupOnce() {
        contact = new Contact(1, "name1", "name1@mail.com", "(111) 111-1111");
        person = new Person("firstName1", "lastName2",
                            Arrays.asList(new Todo("1st", Collections.emptyList()),
                                          new Todo("2nd", Collections.emptyList())));
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstruction() {

        try {

            SqlBuilder SqlBuilder = new SqlBuilder(contact);

            assertEquals("SqlBuilder should have the table name for Type Contact: ", "contacts", SqlBuilder.getTableName());

            List<FieldBuilder> fieldBuilders = SqlBuilder.getFieldBuilders();
            List<FieldBuilder> collectionBuilders = SqlBuilder.getCollectionBuilders();

            assertNotNull(fieldBuilders);
            assertNotNull(collectionBuilders);
            assertEquals("fieldBuilders should have elements: ", 3, fieldBuilders.size());
            assertEquals("collectionBuilders should have no elements: ", 0, collectionBuilders.size());

            assertFieldEquals("name", "name1", "name VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(0));
            assertFieldEquals("email", "name1@mail.com", "email VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(1));
            assertFieldEquals("phone", "(111) 111-1111", "phone VARCHAR(20) NOT NULL", VarCharBuilder.class, fieldBuilders.get(2));

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

        try {

            SqlBuilder SqlBuilder = new SqlBuilder(person);

            assertEquals("SqlBuilder should have the table name for Type Contact: ", "persons", SqlBuilder.getTableName());

            List<FieldBuilder> fieldBuilders = SqlBuilder.getFieldBuilders();
            List<FieldBuilder> collectionBuilders = SqlBuilder.getCollectionBuilders();

            assertNotNull(fieldBuilders);
            assertNotNull(collectionBuilders);
            assertEquals("fieldBuilders should have elements: ", 2, fieldBuilders.size());
            assertEquals("collectionBuilders should have elements: ", 1, collectionBuilders.size());
            assertEquals("collectionBuilders element 1 should have the size of: ", 2, (((Collection<?>) collectionBuilders.get(0).getValue()).size()));

            assertFieldEquals("firstName", "firstName1", "firstName VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(0));
            assertFieldEquals("lastName", "lastName2", "lastName VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuilders.get(1));
            assertCollectionFieldEquals("todos", "[[id: '0', todo: '1st'], [id: '0', todo: '2nd']]", "[NYI]", CollectionBuilder.class, collectionBuilders.get(0));

            List<Persistable> persistables;
            persistables = SqlBuilder.getCollectionBuilders()
                                       .stream()
                                       .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                                       .map(object -> (Persistable) object)
                                       .collect(Collectors.toList());

            assertNotNull(persistables);
            assertEquals("there should be 2 Todos in Person: ", 2, persistables.size());

            SqlBuilder SqlBuilderTodo1 = new SqlBuilder(persistables.get(0));
            List<FieldBuilder> fieldBuildersTodo1 = SqlBuilderTodo1.getFieldBuilders();
            List<FieldBuilder> collectionBuildersTodo1 = SqlBuilderTodo1.getCollectionBuilders();

            assertNotNull(fieldBuildersTodo1);
            assertNotNull(collectionBuildersTodo1);
            assertEquals("fieldBuildersTodo1 should have elements: ", 1, fieldBuildersTodo1.size());
            assertEquals("collectionBuildersTodo1 should have elements: ", 1, collectionBuildersTodo1.size());
            assertEquals("collectionBuildersTodo1s element 1 should have no elements: ", 0, (((Collection<?>) collectionBuildersTodo1.get(0).getValue()).size()));

            assertFieldEquals("todo", "1st", "todo VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuildersTodo1.get(0));

            SqlBuilder SqlBuilderTodo2 = new SqlBuilder(persistables.get(1));
            List<FieldBuilder> fieldBuildersTodo2 = SqlBuilderTodo2.getFieldBuilders();
            List<FieldBuilder> collectionBuildersTodo2 = SqlBuilderTodo2.getCollectionBuilders();

            assertNotNull(fieldBuildersTodo2);
            assertNotNull(collectionBuildersTodo2);
            assertEquals("fieldBuildersTodo2 should have elements: ", 1, fieldBuildersTodo2.size());
            assertEquals("collectionBuildersTodo2 should have elements: ", 1, collectionBuildersTodo2.size());
            assertEquals("collectionBuildersTodo2s element 1 should have no elements: ", 0, (((Collection<?>) collectionBuildersTodo2.get(0).getValue()).size()));

            assertFieldEquals("todo", "2nd", "todo VARCHAR(40) NOT NULL", VarCharBuilder.class, fieldBuildersTodo2.get(0));

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testBuildCreateSql() {

        try {

            String expectedCreateSql = "CREATE TABLE IF NOT EXISTS contacts " +
                                       "(id MEDIUMINT NOT NULL AUTO_INCREMENT, " +
                                       "name VARCHAR(40) NOT NULL, " +
                                       "email VARCHAR(40) NOT NULL, " +
                                       "phone VARCHAR(20) NOT NULL, " +
                                       "PRIMARY KEY(id));";

            SqlBuilder SqlBuilder = new SqlBuilder(contact);

            String builtCreateSql = SqlBuilder.buildCreateTable();

            assertNotNull(builtCreateSql);
            assertEquals("create table should equal to: ", expectedCreateSql, builtCreateSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }
    }

    @Test
    public void testBuildDropSql() {

        try {
            SqlBuilder SqlBuilder = new SqlBuilder(contact);

            String builtDropSql = SqlBuilder.buildDropTable();

            assertNotNull(builtDropSql);
            assertEquals("drop table should equal to: ", "DROP TABLE IF EXISTS contacts;", builtDropSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }

    }

    @Test
    public void testBuildInsert() {

        try {
            String expectedInsertSql = "INSERT INTO contacts (name, email, phone) VALUES(?, ?, ?);";

            SqlBuilder SqlBuilder = new SqlBuilder(contact);

            String builtInsertSql = SqlBuilder.buildInsert();

            assertNotNull(builtInsertSql);
            assertEquals("insert into should equal to: ", expectedInsertSql, builtInsertSql);

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
    public void testBuildUpdate() {

        try {
            String expectedUpdateSql = "UPDATE contacts SET name = ?, email = ?, phone = ?;";

            SqlBuilder SqlBuilder = new SqlBuilder(contact);

            String builtUpdateeSql = SqlBuilder.buildUpdate();

            assertNotNull(builtUpdateeSql);
            assertEquals("update should equal to: ", expectedUpdateSql, builtUpdateeSql);

        } catch (DBValidityException e) {
            fail("SqlBuilder(Persistable persistable) should not create DBValidityExceptions during construction: " + e.getMessage());
        }
    }

    private void assertCollectionFieldEquals(String fieldName, String fieldValue, String sql, Class<?> expectedClass, FieldBuilder fieldBuilder) {
        assertEquals("class should be: ", expectedClass, fieldBuilder.getClass());
        assertEquals(format("fieldBuilder %s: name: ", fieldName), fieldName, fieldBuilder.getName());
        assertEquals(format("fieldBuilder %s: value: ", fieldName), fieldValue, fieldBuilder.getValue().toString());
        assertEquals(format("fieldBuilder %s: sql: ", fieldName), sql, fieldBuilder.getSql());
    }
    private void assertFieldEquals(String fieldName, String fieldValue, String sql, Class<?> expectedClass, FieldBuilder fieldBuilder) {

        assertEquals("class should be: ", expectedClass, fieldBuilder.getClass());
        assertEquals(format("fieldBuilder %s: name: ", fieldName), fieldName, fieldBuilder.getName());
        assertEquals(format("fieldBuilder %s: value: ", fieldName), fieldValue, fieldBuilder.getValue());
        assertEquals(format("fieldBuilder %s: sql: ", fieldName), sql, fieldBuilder.getSql());
    }

}