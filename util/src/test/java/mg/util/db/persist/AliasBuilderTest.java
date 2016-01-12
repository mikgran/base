package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AliasBuilderTest {


    @Test
    public void testTableAliasingConsecutively() {

        AliasBuilder aliasBuilder = new AliasBuilder();

        String contactsAliasCandidate = aliasBuilder.aliasTable("contacts");

        assertNotNull(contactsAliasCandidate);
        assertEquals("alias for contacts should be: ", "c1", contactsAliasCandidate);
        assertEquals("there should be aliases: ", 1, aliasBuilder.aliasCount());
        assertEquals("builder should have an alias for contacts: ", "c1", aliasBuilder.getAlias("contacts"));

        String consumerAliasCandidate = aliasBuilder.aliasTable("consumer");
        assertNotNull(consumerAliasCandidate);
        assertEquals("alias for consumer should be: ", "c2", consumerAliasCandidate);
        assertEquals("there should be aliases: ", 2, aliasBuilder.aliasCount());
        assertEquals("builder should have an alias for consumers: ", "c2", aliasBuilder.getAlias("consumer"));
        assertEquals("builder should have an alias for contacts still: ", "c1", aliasBuilder.getAlias("contacts"));
    }
}
