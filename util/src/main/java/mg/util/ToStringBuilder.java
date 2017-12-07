package mg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import mg.util.validation.Validator;

public class ToStringBuilder<T> {

    private T typeRef;
    private List<String> fieldStrings = new ArrayList<>();

    public static <T> ToStringBuilder<T> of(T typeRef) {
        return new ToStringBuilder<>(typeRef);
    }

    public ToStringBuilder(T typeRef) {
        this.typeRef = Validator.validateNotNull("typeRef", typeRef);
    }

    @SuppressWarnings("unused")
    private ToStringBuilder() {
        // prohibit the use of default constructor
    }

    public ToStringBuilder<T> add(Function<T, String> fun) {
        String str = fun.apply(typeRef);
        Validator.validateNotNull("function return value", str);
        fieldStrings.add(str);
        return this;
    }

    public String build() {

        StringBuffer buffer = new StringBuffer();

        buffer.append(typeRef.getClass().getSimpleName() + "(")
              .append(str)
              .append(")")
;


        return buffer.toString();
    }


}
