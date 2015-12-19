package mg.util.validation.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContainsFieldRuleTest {

    @SuppressWarnings("unused")
    private String nameField = "";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testContainsField() {

        ContainsFieldRule containsFieldRule = new ContainsFieldRule().inType(this);

        assertNotNull(containsFieldRule);
        assertTrue("apply should return true for nameField.", containsFieldRule.apply("nameField"));
        assertFalse("apply should return false for fieldNotInType.", containsFieldRule.apply("fieldNotInType"));
        assertFalse("rule should return false for a null.", containsFieldRule.apply(null));
    }

}
