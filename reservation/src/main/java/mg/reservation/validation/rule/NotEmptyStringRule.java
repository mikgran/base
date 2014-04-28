package mg.reservation.validation.rule;


/**
 * Applies a rule of not null not empty to an object. 
 */
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
		return "can not be empty or null string.";
	}
}