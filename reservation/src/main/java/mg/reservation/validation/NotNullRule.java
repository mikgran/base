package mg.reservation.validation;

public class NotNullRule extends ValidationRule {

	@Override
	public boolean apply(Object object) {

		if (object != null) {
			return true;
		}
		return false;
	}

	@Override
	public String getMessage() {
		return "may not be null.";
	}
}