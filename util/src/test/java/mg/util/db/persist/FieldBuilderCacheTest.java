package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.support.Person;

public class FieldBuilderCacheTest {

    // TOIMPROVE: test coverage: cases: oneToOne
    @Test
    public void testCache() throws DBValidityException {

        Person person = new Person();
        FieldBuilderCache cache = new FieldBuilderCache();

        {
            BuilderInfo personBuilders = cache.buildersFor(person);
            List<FieldBuilder> oneToManyBuilders = personBuilders.getOneToManyBuilders();
            List<FieldBuilder> oneToOneBuilders = personBuilders.getOneToOneBuilders();
            List<FieldBuilder> fieldBuilders = personBuilders.getFieldBuilders();
            List<FieldBuilder> foreignKeyBuilders = personBuilders.getForeignKeyBuilders();
            List<FieldBuilder> idBuilders = personBuilders.getIdBuilders();
            FieldBuilder primaryKeyBuilder = personBuilders.getPrimaryKeyBuilder();

            assertNotNull(personBuilders);
            assertNotNull(oneToManyBuilders);
            assertNotNull(oneToOneBuilders);
            assertNotNull(fieldBuilders);
            assertNotNull(foreignKeyBuilders);
            assertNotNull(idBuilders);
            assertNotNull(primaryKeyBuilder);
            assertEquals("personBuilders.tableName should be: ", "persons", personBuilders.getTableName());
            assertEquals("Class<?> for reftype should be: ", Person.class, personBuilders.getRefType());
            assertEquals("personBuilders.oneToManyBuilders should have the size: ", 1, oneToManyBuilders.size());
            assertEquals("personBuilders.oneToOneBuilders should have the size: ", 0, oneToOneBuilders.size());
            assertEquals("personBuilders.fieldBuilders should have the size: ", 3, fieldBuilders.size());
            assertEquals("personBuilders.foreignKeyBuilders should have the size: ", 0, foreignKeyBuilders.size());
            assertEquals("personBuilders.idBuilders should have the size: ", 1, idBuilders.size());
            assertEquals("personBuilders.primaryKeyBuilder.name should be: ", "id", primaryKeyBuilder.getName());

            assertTrue("personBuilders.oneToManyBuilders should have a builder for field todos.",
                       oneToManyBuilders.stream().anyMatch(cb -> cb.getName().equals("todos")));

            assertTrue("personBuilders.fieldBuilders should have a builder for field firstName.",
                       fieldBuilders.stream().anyMatch(fb -> fb.getName().equals("firstName")));

            assertTrue("personBuilders.fieldBuilders should have a builder for field id.",
                       fieldBuilders.stream().anyMatch(fb -> fb.getName().equals("id")));

            assertTrue("personBuilders.fieldBuilders should have a builder for field lastName.",
                       fieldBuilders.stream().anyMatch(fb -> fb.getName().equals("lastName")));

            assertTrue("personBuilders.idBuilders should have a builder for field id.",
                       idBuilders.stream().anyMatch(fb -> fb.getName().equals("id")));
        }
        {
            // afterwards the cache should have a BuilderInfo for Person.class:
            assertEquals("cache should have entries: ", 1, cache.getBuilders().size());
            BuilderInfo personBuilders = cache.getBuilders().get(Person.class);
            List<FieldBuilder> collectionBuilders = personBuilders.getOneToManyBuilders();
            List<FieldBuilder> fieldBuilders = personBuilders.getFieldBuilders();
            List<FieldBuilder> foreignKeyBuilders = personBuilders.getForeignKeyBuilders();
            List<FieldBuilder> idBuilders = personBuilders.getIdBuilders();
            FieldBuilder primaryKeyBuilder = personBuilders.getPrimaryKeyBuilder();

            assertNotNull(personBuilders);
            assertNotNull(collectionBuilders);
            assertNotNull(fieldBuilders);
            assertNotNull(foreignKeyBuilders);
            assertNotNull(idBuilders);
            assertNotNull(primaryKeyBuilder);
            assertEquals("personBuilders.tableName should be: ", "persons", personBuilders.getTableName());
            assertEquals("Class<?> for reftype should be: ", Person.class, personBuilders.getRefType());
            assertEquals("personBuilders.oneToManyBuilders should have the size: ", 1, collectionBuilders.size());
            assertEquals("personBuilders.fieldBuilders should have the size: ", 3, fieldBuilders.size());
            assertEquals("personBuilders.foreignKeyBuilders should have the size: ", 0, foreignKeyBuilders.size());
            assertEquals("personBuilders.idBuilders should have the size: ", 1, idBuilders.size());
            assertEquals("personBuilders.primaryKeyBuilder.name should be: ", "id", primaryKeyBuilder.getName());

            assertTrue("personBuilders.oneToManyBuilders should have a builder for field todos.",
                       collectionBuilders.stream().anyMatch(cb -> cb.getName().equals("todos")));

            assertTrue("personBuilders.fieldBuilders should have a builder for field firstName.",
                       fieldBuilders.stream().anyMatch(fb -> fb.getName().equals("firstName")));

            assertTrue("personBuilders.fieldBuilders should have a builder for field id.",
                       fieldBuilders.stream().anyMatch(fb -> fb.getName().equals("id")));

            assertTrue("personBuilders.fieldBuilders should have a builder for field lastName.",
                       fieldBuilders.stream().anyMatch(fb -> fb.getName().equals("lastName")));

            assertTrue("personBuilders.idBuilders should have a builder for field id.",
                       idBuilders.stream().anyMatch(fb -> fb.getName().equals("id")));
        }

    }

}
