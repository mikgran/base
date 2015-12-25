package mg.util.functional.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> extends Function<T, R> {

    @Override
    default public R apply(T t) {

        try {

            return applyThrows(t);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    // TOIMPROVE: test this
    default <V> ThrowingFunction<T, V, E> andThen(ThrowingFunction<? super R, ? extends V, E> after) throws E {
        Objects.requireNonNull(after);
        return (T t) -> after.applyThrows(applyThrows(t));
    }

    R applyThrows(T t) throws E;

    // TOIMPROVE: test this
    default <V> ThrowingFunction<V, R, E> compose(ThrowingFunction<? super V, ? extends T, E> before) throws E {
        Objects.requireNonNull(before);
        return (V v) -> applyThrows(before.applyThrows(v));
    }
}
