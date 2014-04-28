package mg.reservation.validation.rule;


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
		return "can not be null.";
	}
}