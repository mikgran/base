package mg.util.functional.supplier;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> extends Supplier<T> {

    // TOIMPROVE: tests !
    @Override
    default T get() {

        try {

            return getThrows();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    T getThrows() throws E;
}

