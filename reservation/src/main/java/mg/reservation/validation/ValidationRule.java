package mg.reservation.validation;

public abstract class ValidationRule {

	// a collection of rules
	public static final NotNullRule NOT_NULL = new NotNullRule();
	public static final NotEmptyStringRule NOT_EMPTY_STRING = new NotEmptyStringRule();

	public abstract boolean apply(Object object);

	public abstract String getMessage();
}
