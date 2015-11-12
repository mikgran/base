package mg.util.validation.rule;

/**
 * Tests whether the applied object is not negative and not zero.
 */
public class NotNegativeOrZeroNumberRule extends ValidationRule {

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
		return "can not be a zero or negative number";
	}
}
