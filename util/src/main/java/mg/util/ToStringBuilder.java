package mg.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import mg.util.validation.Validator;

public class ToStringBuilder<T> {

    private T typeRef;
    private List<String> fieldStrings = new ArrayList<>();
    private Function<ToStringBuilder<T>, String> buildFunction;

    /**
     * Builds a toString using lambdas.
     * <pre>
     * ToStringBuilder.of(new TestClass())
     *                .add(t -> t.field1)
     *                .add(t -> t.field2)
     *                .add(t -> t.field3)
     *                .build()
     * </pre>
     * @param typeRef type to build the toString for.
     * @return the String representation of an Object, typically in the form of: ClassName(value1, value2, value3)
     */
    public static <T> ToStringBuilder<T> of(T typeRef) {
        ToStringBuilder<T> toStringBuilder = new ToStringBuilder<>(typeRef);
        toStringBuilder.buildFunction = ToStringBuilder::buildNormal;
        return toStringBuilder;
    }

    /**
     * Builds a toString using reflection.
     * <pre>
     * ToStringBuilder.reflectiveOf(new TestClass())
     *                .build()
     * </pre>
     * @param typeRef type to build the toString for.
     * @return the String representation of an Object, typically in the form of: ClassName(value1, value2, value3)
     */
    public static <T> ToStringBuilder<T> reflectiveOf(T typeRef) {
        ToStringBuilder<T> toStringBuilder = new ToStringBuilder<>(typeRef);
        toStringBuilder.buildFunction = ToStringBuilder::buildReflective;
        return toStringBuilder;
    }

    private static <T> String buildFieldString(T typeReference, Field field) {
        StringBuffer buffer = new StringBuffer();

        try {
            field.setAccessible(true);

            buffer.append(field.getName())
                  .append(": '")
                  .append(field.get(typeReference))
                  .append("'");

        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TOIMPROVE: logging!
            // result in an empty field name and value.
        }

        return buffer.toString();
    }

    private static <T> String buildNormal(ToStringBuilder<T> toStringBuilder) {
        String prefix = getPrefix(toStringBuilder);
        String postfix = getPostfix();

        StringJoiner joiner = new StringJoiner(", ", prefix, postfix);
        joiner.setEmptyValue("''");

        toStringBuilder.fieldStrings.stream()
                                    .forEach(joiner::add);

        return joiner.toString();
    }

    private static <T> String buildReflective(ToStringBuilder<T> toStringBuilder) {

        String prefix = getPrefix(toStringBuilder);
        String postfix = getPostfix();

        StringJoiner joiner = new StringJoiner(", ", prefix, postfix);
        joiner.setEmptyValue("''");

        T typeReference = toStringBuilder.typeRef;
        List<Field> fields = Arrays.asList(typeReference.getClass().getDeclaredFields());

        fields.stream()
              .forEach(field -> {
                  Optional.ofNullable(buildFieldString(typeReference, field))
                          .ifPresent(joiner::add);
              });

        return joiner.toString();
    }

    private static String getPostfix() {
        return ")";
    }

    private static <T> String getPrefix(ToStringBuilder<T> toStringBuilder) {
        return toStringBuilder.typeRef.getClass().getSimpleName() + "(";
    }

    @SuppressWarnings("unused")
    private ToStringBuilder() {
        throw new IllegalAccessError("use *of(typeRef) instead.");
    }

    private ToStringBuilder(T typeRef) {
        this.typeRef = Validator.validateNotNull("typeRef", typeRef);
    }

    public ToStringBuilder<T> add(Function<T, String> function) {
        Validator.validateNotNull("function", function);

        String functionResult = function.apply(typeRef);
        Optional.ofNullable(functionResult)
                .ifPresent(fieldStrings::add);

        return this;
    }

    public String build() {
        return buildFunction.apply(this);
    }

}
