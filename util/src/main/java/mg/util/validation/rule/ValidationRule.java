package mg.util.validation.rule;

public abstract class ValidationRule {

    // a collection of rules for convenient static accessing: Validator.of("field", field, NOT_NULL).validate();
    public static final NotNullRule NOT_NULL = new NotNullRule();
    public static final NotNullOrEmptyStringRule NOT_NULL_OR_EMPTY_STRING = new NotNullOrEmptyStringRule();
    public static final DateConstraintRule DATE_CONSTRAINT = new DateConstraintRule();
    public static final DateEarlierRule DATE_EARLIER = new DateEarlierRule();
    public static final NotNegativeOrZeroNumberRule NOT_NEGATIVE_OR_ZERO = new NotNegativeOrZeroNumberRule();
    public static final NotNegativeOrZeroStringAsNumberRule NOT_NEGATIVE_OR_ZERO_AS_STRING = new NotNegativeOrZeroStringAsNumberRule();
    public static final NumberOfCharactersRule NUMBER_OF_CHARACTERS = new NumberOfCharactersRule();
    public static final ConnectionNotClosedRule CONNECTION_NOT_CLOSED = new ConnectionNotClosedRule();
    public static final ContainsFieldRule CONTAINS_FIELD = new ContainsFieldRule();
    public static final FieldTypeMatchesRule FIELD_TYPE_MATCHES = new FieldTypeMatchesRule();

    public static ConnectionNotClosedRule connectionNotClosed() {
        return CONNECTION_NOT_CLOSED;
    }

    public static ContainsFieldRule containsField() {
        return CONTAINS_FIELD;
    }

    public static DateConstraintRule dateConstraint() {
        return DATE_CONSTRAINT;
    }

    public static DateEarlierRule dateEarlier() {
        return DATE_EARLIER;
    }

    public static FieldTypeMatchesRule fieldTypeMatches() {
        return FIELD_TYPE_MATCHES;
    }

    public static NotNegativeOrZeroNumberRule notNegativeOrZero() {
        return NOT_NEGATIVE_OR_ZERO;
    }

    public static NotNegativeOrZeroStringAsNumberRule notNegativeOrZeroAsString() {
        return NOT_NEGATIVE_OR_ZERO_AS_STRING;
    }

    // TOCONSIDER: moving most common rules into Validator or creating methods for each rule in the abstract class.
    // Note to self: all capitals look hor-ri-bad. They just do.
    public static ValidationRule notNull() {
        return NOT_NULL;
    }

    public static ValidationRule notNullOrEmptyString() {
        return NOT_NULL_OR_EMPTY_STRING;
    }

    public static NumberOfCharactersRule numberOfCharacters() {
        return NUMBER_OF_CHARACTERS;
    }

    public abstract boolean apply(Object object);

    public abstract String getMessage();
}
