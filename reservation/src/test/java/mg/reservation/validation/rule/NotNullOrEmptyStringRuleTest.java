package mg.reservation.validation.rule;

import static org.junit.Assert.*;

import org.junit.Test;

public class NotNullOrEmptyStringRuleTest {

	@Test
	public void testNotNullOrEmpty() {
		
		NotNullOrEmptyStringRule notNullOrEmptyStringRule = new NotNullOrEmptyStringRule();
		
		boolean ruleApplies = notNullOrEmptyStringRule.apply("a");
		assertTrue("applying to 'a' should be true", ruleApplies);
		
		ruleApplies = notNullOrEmptyStringRule.apply("");
		assertTrue("applying to an empty string should be false", !ruleApplies);
		
		ruleApplies = notNullOrEmptyStringRule.apply((String)null);
		assertTrue("applying to null should be false", !ruleApplies);
	}
}
