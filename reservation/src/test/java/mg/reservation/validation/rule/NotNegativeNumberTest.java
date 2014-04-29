package mg.reservation.validation.rule;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests whether the applied number is 1 or higher. Zero and negatives produce false.
 */
public class NotNegativeNumberTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testNotNegativeRule() {

		boolean ruleApplies = new NotNegativeNumberRule().apply(1);
		assertTrue("can not be a negative number.", ruleApplies);

		ruleApplies = new NotNegativeNumberRule().apply(-1);
		assertTrue("can not be a positive number.", !ruleApplies);

		ruleApplies = new NotNegativeNumberRule().apply(0);
		assertTrue("can not be a negative number.", !ruleApplies);
	}

}
