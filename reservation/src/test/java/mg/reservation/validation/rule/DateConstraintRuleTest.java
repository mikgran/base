package mg.reservation.validation.rule;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;

import mg.reservation.util.Common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DateConstraintRuleTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testBetweenConstraints() throws ParseException {

		DateConstraintRule dateRule = DateConstraintRule.forDates("2010-10-01 00:00", "2010-10-01 01:00");
		boolean ruleValid = dateRule.apply(dateFrom("2010-10-01 00:30"));
		assertTrue("given date should be between the constraints", ruleValid);
	}

	@Test
	public void testOutsideConstraints() throws ParseException {

		DateConstraintRule dateRule = DateConstraintRule.forDates("2010-10-01 00:00", "2010-10-01 01:00");
		boolean ruleValid = dateRule.apply(dateFrom("2010-09-01 00:00"));
		assertTrue("given date should be outside the constraints", !ruleValid);

		ruleValid = dateRule.apply(dateFrom("2010-11-01 00:00"));
		assertTrue("given date should be outside the constraints", !ruleValid);
	}

	private Date dateFrom(String dateString) throws ParseException {
		return Common.yyyyMMddHHmmFormatter.parse(dateString);
	}
}
