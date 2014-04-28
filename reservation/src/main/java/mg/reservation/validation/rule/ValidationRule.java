package mg.reservation.validation.rule;


public abstract class ValidationRule {

	// a collection of rules
	public static final NotNullRule NOT_NULL = new NotNullRule();
	public static final NotEmptyStringRule NOT_EMPTY_STRING = new NotEmptyStringRule();
	public static final DateConstraintRule DATE_CONSTRAINT = new DateConstraintRule();
	public static final DateEarlierRule DATE_EARLIER = new DateEarlierRule();

	public abstract boolean apply(Object object);

	public abstract String getMessage();
}
