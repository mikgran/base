package mg.util.validation.rule;

import java.util.Objects;
import java.util.stream.Stream;

public class ContainsFieldRule extends ValidationRule {

    private Object o = null;
    private String fieldName = "";

    public ContainsFieldRule() {
    }

    private ContainsFieldRule(Object o) {
        this.o = Objects.requireNonNull(o, "o can not be null.");
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
