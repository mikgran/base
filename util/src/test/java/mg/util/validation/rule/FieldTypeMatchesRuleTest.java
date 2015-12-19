package mg.util.validation.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FieldTypeMatchesRuleTest {

    @SuppressWarnings("unused")
    private String nameField = "";

    @SuppressWarnings("unused")
    private int intField = 0;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // mvn -DfailIfNoTests=false -Dtest=FieldTypeMatchesRuleTest#testFieldMatches test
    @Test
    public void testFieldMatches() {

        FieldTypeMatchesRule fieldTypeMatchesRule1 = new FieldTypeMatchesRule().inType(this, "nameField");

        assertNotNull(fieldTypeMatchesRule1);
        assertTrue("apply should return true for nameField.", fieldTypeMatchesRule1.apply("stringData"));
        assertFalse("apply should return false for intField.", fieldTypeMatchesRule1.apply(0));
        assertFalse("apply should return false for a null.", fieldTypeMatchesRule1.apply(null));

        FieldTypeMatchesRule fieldTypeMatchesRule2 = new FieldTypeMatchesRule().inType(this, "intField");

        assertNotNull(fieldTypeMatchesRule2);
        assertFalse("apply should return false for nameField.", fieldTypeMatchesRule2.apply("stringData"));
        assertTrue("apply should return true for intField.", fieldTypeMatchesRule2.apply(0));
        assertFalse("apply should return false for a null.", fieldTypeMatchesRule2.apply(null));

    }

}