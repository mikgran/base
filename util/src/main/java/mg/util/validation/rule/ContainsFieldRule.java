package mg.util.validation.rule;

import static mg.util.validation.Validator.validateNotNull;

import java.util.stream.Stream;

public class ContainsFieldRule extends ValidationRule {

    private Object o = null;
    private String fieldName = "";

    public ContainsFieldRule() {
    }

    private ContainsFieldRule(Object o) {
        this.o = validateNotNull("o", o);
    }

    @Override
    public boolean apply(Object object) {

        if (object == null || o == null) {
            return false;
        }

        this.fieldName = object.toString();

        return Stream.of(o.getClass().getDeclaredFields())
                     .anyMatch(field -> field.getName().equals(object.toString()));
    }

    @Override
    public String getMessage() {
        return "No field named " + fieldName;
    }

    public ContainsFieldRule inType(Object object) {
        return new ContainsFieldRule(object);
    }
}
