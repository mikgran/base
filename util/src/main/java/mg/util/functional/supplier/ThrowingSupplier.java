package mg.util.functional.supplier;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> extends Supplier<T> {

    public static <T, X extends Exception> ThrowingSupplier<T, X> of(ThrowingSupplier<T, X> supplier) {
        ThrowingSupplier<T, X> trowingSupplier = () -> supplier.get();
        return trowingSupplier;
    }

    public static <T, X extends Exception> Supplier<T> supplierOf(ThrowingSupplier<T, X> supplier) {
        return supplier;
    }

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

