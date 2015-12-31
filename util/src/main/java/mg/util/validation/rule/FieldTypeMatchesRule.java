package mg.util.validation.rule;

import static mg.util.Common.isInterchangeable;
import static mg.util.validation.Validator.validateNotNull;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public class FieldTypeMatchesRule extends ValidationRule {

    private String fieldName = "";
    private Object o;
    private String typeInfo = "";

    public FieldTypeMatchesRule() {
    }

    private FieldTypeMatchesRule(Object o, String fieldName) {
        this.o = validateNotNull("o", o);
        this.fieldName = validateNotNull("fieldName", fieldName);
    }

    @Override
    public boolean apply(Object object) {

        if (object == null) {
            typeInfo = "object was null.";
            return false;
        }

        Optional<Field> fieldCandidate = Arrays.stream(o.getClass().getDeclaredFields())
                                               .filter(field -> field.getName().equals(fieldName))
                                               .findFirst();

        // Integer, Float, Double, etc, gah, WTS idiot reflection system or idiot coder.
        // There has to be a better way to primitive versus boxed equi, there just
        // has to be, this is worse than the O(worst ever) code I write, this sets the
        // new low for me.
        if (fieldCandidate.isPresent()) {

            Class<?> candidateType = fieldCandidate.get().getType();

            if (isInterchangeable(object, candidateType)) {

                return true;
            }

            if (candidateType.equals(object.getClass())) {
                return true;
            }

            // cross matching allowed with: Dates and LocalDateTimes -> interchangeable via Common.toDate or Common.toLocalDateTime
            if (object instanceof Date && candidateType == LocalDateTime.class ||
                object instanceof LocalDateTime && candidateType == Date.class) {
                return true;
            }

            typeInfo = "field type: " + candidateType.getSimpleName();
        }
        return false;
    }

    @Override
    public String getMessage() {
        return "does not match the class of the field in the given object: " + typeInfo;
    }

    public FieldTypeMatchesRule inType(Object object, String fieldName) {
        return new FieldTypeMatchesRule(object, fieldName);
    }
}
