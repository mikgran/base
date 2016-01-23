package mg.util.functional.predicate;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowingPredicate<T, E extends Exception> extends Predicate<T> {

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
    default ThrowingPredicate<T, E> negate() {
        return (t) -> !testThrows(t);
    }

    // TOIMPROVE: test coverage
    default ThrowingPredicate<T, E> or(ThrowingPredicate<? super T, E> other) {
        Objects.requireNonNull(other);
        return (t) -> testThrows(t) || other.testThrows(t);
    }
}
