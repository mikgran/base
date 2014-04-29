package mg.reservation.validation.rule;

public class NotNegativeNumberRule extends ValidationRule {

	@Override
	public boolean apply(Object object) {

		long number = 0;

		if (object != null) {

			if (object instanceof Integer) {
				number = (Integer) object;
			}
			if (object instanceof Long) {
				number = (Long) object;
			}
			if (number > 0) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getMessage() {
		return "can not be a negative number.";
	}
}
