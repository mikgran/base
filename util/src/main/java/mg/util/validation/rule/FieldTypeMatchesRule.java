package mg.util.validation.rule;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class FieldTypeMatchesRule extends ValidationRule {

    private Object o;
    private String typeInfo = "";
    private String fieldName = "";

    public FieldTypeMatchesRule() {
    }

    private FieldTypeMatchesRule(Object o, String fieldName) {
        this.o = Objects.requireNonNull(o, "o can not be null.");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName can not be null.");
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

            if ((object instanceof Integer && Integer.TYPE == candidateType) ||
                (object instanceof Long && Long.TYPE == candidateType) ||
                (object instanceof Float && Float.TYPE == candidateType) ||
                (object instanceof Double && Double.TYPE == candidateType) ||
                (object instanceof Short && Short.TYPE == candidateType) ||
                (object instanceof Byte && Byte.TYPE == candidateType) ||
                (object instanceof Character && Character.TYPE == candidateType)) {

                return true;
            }

            if (candidateType.equals(object.getClass())) {
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
