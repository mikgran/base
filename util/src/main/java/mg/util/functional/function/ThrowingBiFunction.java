package mg.util.functional.function;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Exception> extends BiFunction<T, U, R> {

    default public <V> ThrowingBiFunction<T, U, V, E> andThen(ThrowingFunction<? super R, ? extends V, E> after) {
        // return BiFunction.super.andThen(after);
        Objects.requireNonNull(after);
        return (T t, U u) -> after.applyThrows(applyThrows(t, u));
    }

    @Override
    default public R apply(T t, U u) {

        try {

            return applyThrows(t, u);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t, U u) throws E;
}
