package mg.reservation.validation.rule;

import java.text.ParseException;
import java.util.Date;

import mg.util.Common;

public class DateConstraintRule extends ValidationRule {

	private Date lowerConstraint = null;
	private Date upperConstraint = null;

	public DateConstraintRule() {
	}

	public DateConstraintRule(Date lowerConstraint, Date upperConstraint) {
		this.lowerConstraint = lowerConstraint;
		this.upperConstraint = upperConstraint;
	}

	@Override
	public boolean apply(Object object) {

		if (object != null
				&& object instanceof Date
				&& lowerConstraint != null
				&& upperConstraint != null) {

			Date candidate = ((Date) object);

			if (candidate.getTime() >= lowerConstraint.getTime() &&
					candidate.getTime() <= upperConstraint.getTime()) {

				return true;
			}
		}
		return false;
	}

	@Override
	public String getMessage() {
		return "can not be empty or null string.";
	}

	public DateConstraintRule forDates(String lowerConstraint, String upperConstraint) throws ParseException {
		Date lowerBoundary = Common.yyyyMMddHHmmFormatter.parse(lowerConstraint);
		Date upperBoundary = Common.yyyyMMddHHmmFormatter.parse(upperConstraint);		
		return new DateConstraintRule(lowerBoundary, upperBoundary);
	}

}
