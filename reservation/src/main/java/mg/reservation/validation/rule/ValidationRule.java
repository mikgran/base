package mg.reservation.validation.rule;

public abstract class ValidationRule {

	// a collection of rules for convenient static accessing: new Validator().add("field", field, NOT_NULL).validate();
	public static final NotNullRule NOT_NULL = new NotNullRule();
	public static final NotNullOrEmptyStringRule NOT_NULL_OR_EMPTY_STRING = new NotNullOrEmptyStringRule();
	public static final DateConstraintRule DATE_CONSTRAINT = new DateConstraintRule();
	public static final DateEarlierRule DATE_EARLIER = new DateEarlierRule();
	public static final NotNegativeOrZeroNumberRule NOT_NEGATIVE_OR_ZERO = new NotNegativeOrZeroNumberRule();

	public abstract boolean apply(Object object);

	public abstract String getMessage();
}
