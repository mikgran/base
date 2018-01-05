package mg.util.functional.predicate;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowingPredicate<T, E extends Exception> extends Predicate<T> {

    /**
     * Wraps a normal function into a ThrowingPredicate.
     */
    public static <T, X extends Exception> ThrowingPredicate<T, X> of(Predicate<T> predicate) {
        ThrowingPredicate<T, X> throwingPredicate = (T t) -> predicate.test(t);
        return throwingPredicate;
    }

    public static <T, X extends Exception> Predicate<T> predicateOf(ThrowingPredicate<T, X> predicate) {
        return predicate;
    }

    @Override
    default public boolean test(T t) {

        try {

            return testThrows(t);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public boolean testThrows(T t) throws E;

    // TOIMPROVE: test coverage
    default ThrowingPredicate<T, E> and(ThrowingPredicate<? super T, E> other) {
        Objects.requireNonNull(other);
        return (t) -> testThrows(t) && other.testThrows(t);
    }

    // TOIMPROVE: test coverage
    @Override
    default ThrowingPredicate<T, E> negate() {
        return (t) -> !testThrows(t);
    }

    // TOIMPROVE: test coverage
    default ThrowingPredicate<T, E> or(ThrowingPredicate<? super T, E> other) {
        Objects.requireNonNull(other);
        return (t) -> testThrows(t) || other.testThrows(t);
    }
}
