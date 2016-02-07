package mg.util.functional.consumer;

import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U, E extends Exception> extends BiConsumer<T, U> {

    @Override
    default void accept(T t, U u) {

        try {

            acceptThrows(t, u);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T t, U u) throws E;

    default ThrowingBiConsumer<T, U, E> andThen(ThrowingBiConsumer<? super T, ? super U, E> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> {
            acceptThrows(t, u);
            after.accept(t, u);
        };

    }

}
