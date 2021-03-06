package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AliasBuilderTest {

    @Test
    public void testTableAliasingConsecutively() {

        AliasBuilder aliasBuilder = new AliasBuilder();

        String contactsAliasCandidate = aliasBuilder.aliasOf("contacts");

        assertNotNull(contactsAliasCandidate);
        assertEquals("alias for contacts should be: ", "c1", contactsAliasCandidate);
        assertEquals("there should be alias families: ", 1, aliasBuilder.familyCount());
        assertEquals("there should be aliases: ", 1, aliasBuilder.aliasCount());
        assertEquals("builder should have an alias for contacts: ", "c1", aliasBuilder.aliasOf("contacts"));

        String consumerAliasCandidate = aliasBuilder.aliasOf("consumers");

        assertNotNull(consumerAliasCandidate);
        assertEquals("alias for consumer should be: ", "c2", consumerAliasCandidate);
        assertEquals("there should be alias families: ", 1, aliasBuilder.familyCount());
        assertEquals("there should be aliases: ", 2, aliasBuilder.aliasCount());
        assertEquals("builder should have an alias for contacts still: ", "c1", aliasBuilder.aliasOf("contacts"));
        assertEquals("builder should have an alias for consumers: ", "c2", aliasBuilder.aliasOf("consumers"));

        String deliveriesAliasCandidate = aliasBuilder.aliasOf("deliveries");

        assertNotNull(deliveriesAliasCandidate);
        assertEquals("alias for deliveries should be: ", "d1", deliveriesAliasCandidate);
        assertEquals("there should be alias families: ", 2, aliasBuilder.familyCount());
        assertEquals("there should be aliases: ", 3, aliasBuilder.aliasCount());
        assertEquals("builder should have an alias for contacts still: ", "c1", aliasBuilder.aliasOf("contacts"));
        assertEquals("builder should have an alias for consumers still: ", "c2", aliasBuilder.aliasOf("consumers"));
        assertEquals("builder should have an alias for deliveries: ", "d1", aliasBuilder.aliasOf("deliveries"));

        String contactsAliasCadidateB = aliasBuilder.aliasOf("contacts"); // retry for an existing id
        assertNotNull(contactsAliasCadidateB);
        assertEquals("alias for contacts should be: ", "c1", contactsAliasCandidate);
        assertEquals("there should be alias families: ", 2, aliasBuilder.familyCount());
        assertEquals("there should be aliases: ", 3, aliasBuilder.aliasCount());
        assertEquals("builder should have an alias for contacts still: ", "c1", aliasBuilder.aliasOf("contacts"));
        assertEquals("builder should have an alias for consumers still: ", "c2", aliasBuilder.aliasOf("consumers"));
        assertEquals("builder should have an alias for deliveries still: ", "d1", aliasBuilder.aliasOf("deliveries"));
    }
}
