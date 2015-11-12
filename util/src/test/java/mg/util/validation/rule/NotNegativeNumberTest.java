package mg.util.validation.rule;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.validation.rule.NotNegativeOrZeroNumberRule;

/**
 * Tests whether the applied number is 1 or higher. Zero and negatives produce false.
 */
public class NotNegativeNumberTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testNotNegativeRule() {

		boolean ruleApplies = new NotNegativeOrZeroNumberRule().apply(1);
		assertTrue("applying to 1 should return true.", ruleApplies);

		ruleApplies = new NotNegativeOrZeroNumberRule().apply(-1);
		assertTrue("applying to -1 should return false.", !ruleApplies);

		ruleApplies = new NotNegativeOrZeroNumberRule().apply(0);
		assertTrue("applying to 0 should return false.", !ruleApplies);
	}

}
