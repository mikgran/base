package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import mg.util.db.persist.support.Person;

public class FieldBuilderCacheTest {

    @Test
    public void testCache() throws DBValidityException {

        Person person = new Person();
        FieldBuilderCache cache = new FieldBuilderCache();

        BuilderInfo personBuilders = cache.buildersFor(person);

        /*
            private List<FieldBuilder> collectionBuilders;
            private List<FieldBuilder> fieldBuilders;
            private List<FieldBui§lder> foreignKeyBuilders;
            private List<FieldBuilder> idBuilders;
            private FieldBuilder primaryKeyBuilder;
            private Class<?> refType;
            private String tableName;
         */

        assertNotNull(personBuilders);
        assertEquals("personBuilders. should have the size: ", 1, personBuilders.getCollectionBuilders().size());
        assertEquals("personBuilders. should have the size: ", 2, personBuilders.getFieldBuilders().size());
        assertEquals("personBuilders. should have the size: ", 0, personBuilders.getForeignKeyBuilders().size());
        assertEquals("personBuilders. should have the size: ", 1, personBuilders.getIdBuilders().size());
        assertEquals("personBuilders.primaryKeyBuilder.name should be: ", "id", personBuilders.getPrimaryKeyBuilder().getName());

    }

}
