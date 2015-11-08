package mg.reservation.validation.rule;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.Common;

public class DatesOrderedRuleTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAppliedDateBeforeThanDate() throws ParseException {

		DateEarlierRule orderRule = new DateEarlierRule().than(dateFrom("2010-10-01 00:00"));
		boolean ruleApplies = orderRule.apply(dateFrom("2010-09-01 00:00"));
		assertTrue("applied date should be before the other.", ruleApplies);
	}
	
	@Test
	public void testAppliedDateAfterThanDate() throws ParseException {

		DateEarlierRule orderRule = new DateEarlierRule().than(dateFrom("2010-10-01 00:00"));
		boolean ruleApplies = orderRule.apply(dateFrom("2010-10-02 00:00"));
		assertTrue("applied date should be after the other.", !ruleApplies);
	}

	private Date dateFrom(String dateString) throws ParseException {
		return Common.yyyyMMddHHmmFormatter.parse(dateString);
	}

}
