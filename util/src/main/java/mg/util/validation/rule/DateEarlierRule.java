package mg.util.validation.rule;

import java.util.Date;

/**
 * A validation rule for testing whether the dateAfter is later than given date and both are not null. <br />
 * Usage: new Validator().add("date", new Date(longToday), DATE_EARLIER.than(new Date(longTomorrow))).validate();
 */
public class DateEarlierRule extends ValidationRule {

	private Date afterDate = null;

	public DateEarlierRule() {
	}

	private DateEarlierRule(Date afterDate) {
		this.afterDate = afterDate;
	}

	/**
	 * Tests whether the given object, which is assumed to be Date, is before the afterDate.
	 */
	@Override
	public boolean apply(Object object) {

		if (object == null || afterDate == null) {
			return false;
		}

		if (object instanceof Date
				&& afterDate.getTime() > ((Date) object).getTime()) {

			return true;
		}
		return false;
	}

	@Override
	public String getMessage() {
		return "applied date can not be after than afterDate";
	}

	public DateEarlierRule than(Date afterDate) {
		return new DateEarlierRule(afterDate);
	}
}