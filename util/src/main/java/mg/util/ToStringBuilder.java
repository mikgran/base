package mg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import mg.util.validation.Validator;

public class ToStringBuilder<T> {

    private T typeRef;
    private List<String> fieldStrings = new ArrayList<>();
    private boolean reflectiveBuild = true;
    private Function<T, String> buildMethod;

    public static <T> ToStringBuilder<T> of(T typeRef) {
        ToStringBuilder<T> toStringBuilder = new ToStringBuilder<>(typeRef);
        toStringBuilder.buildMethod = ToStringBuilder::buildNormal;
        return toStringBuilder;
    }

    public static <T> ToStringBuilder<T> reflectiveOf(T typeRef) {
        return new ToStringBuilder<>(typeRef).reflective();
    }

    private static <T> String buildNormal(ToStringBuilder<T> toStringBuilder) {
        String prefix = toStringBuilder.typeRef.getClass().getSimpleName() + "(";
        String suffix = ")";

        StringJoiner joiner = new StringJoiner(", ", prefix, suffix);

        joiner.setEmptyValue("''");

        toStringBuilder.fieldStrings.stream()
                                    .forEach(joiner::add);

        return joiner.toString();
    }

    private static <T> String buildReflective(ToStringBuilder<T> toStringBuilder) {
        return "";
    }

    @SuppressWarnings("unused")
    private ToStringBuilder() {
        throw new IllegalAccessError("use *of instead.");
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

        String result;

        //        if (reflectiveBuild) {
        //            result = buildreflective();
        //        } else {
        //            result = buildNormal();
        //        }

        result = buildNormal(typeRef);

        return result;
    }

    private ToStringBuilder<T> reflective() {
        this.reflectiveBuild = true;
        return this;
    }

}
