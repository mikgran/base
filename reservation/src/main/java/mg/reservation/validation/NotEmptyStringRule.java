package mg.reservation.validation;

public class NotEmptyStringRule extends ValidationRule {

	@Override
	public boolean apply(Object object) {

		if (object != null
				&& object instanceof String
				&& ((String) object).length() > 0) {

			return true;
		}
		return false;
	}

	@Override
	public String getMessage() {
		return "may not be an empty string.";
	}
}