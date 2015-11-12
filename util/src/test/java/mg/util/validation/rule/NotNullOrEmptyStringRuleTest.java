package mg.util.validation.rule;

import static org.junit.Assert.*;

import org.junit.Test;

import mg.util.validation.rule.NotNullOrEmptyStringRule;

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
