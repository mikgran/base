package mg.util.functional.option;

import static mg.util.functional.function.ThrowingFunction.functionOf;
import static mg.util.functional.predicate.ThrowingPredicate.predicateOf;
import static mg.util.functional.supplier.ThrowingSupplier.supplierOf;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import mg.util.functional.function.ThrowingFunction;
import mg.util.functional.predicate.ThrowingPredicate;
import mg.util.functional.supplier.ThrowingSupplier;

public class BiOpt<T, U> {

    private static final BiOpt<?, ?> EMPTY = new BiOpt<>();
    private final Opt<T> left;
    private final Opt<U> right;

    @SuppressWarnings("unchecked")
    public static <T, U> BiOpt<T, U> empty() {
        return (BiOpt<T, U>) EMPTY;
    }

    public static <T, U> BiOpt<T, U> of(BiOpt<T, U> other) {
        return new BiOpt<>(other.left, other.right);
    }

    public static <T, U> BiOpt<T, U> of(Opt<T> left, Opt<U> right) {
        return new BiOpt<>(left, right);
    }

    public static <T, U> BiOpt<T, U> of(T left, U right) {
        return new BiOpt<>(left, right);
    }

    private BiOpt() {
        this.left = Opt.empty();
        this.right = Opt.empty();
    }

    private BiOpt(Opt<T> left, Opt<U> right) {
        this.left = Opt.of(left);
        this.right = Opt.of(right);
    }

    private BiOpt(T left, U right) {
        this.left = Opt.of(left);
        this.right = Opt.of(right);
    }

    /**
     * Performs predicate.test on the value of the left. The result is stored on the right.
     */
    public BiOpt<T, ?> filterLeft(Predicate<? super T> predicate) {
        Opt<T> filtered = left.filter(predicate);
        return BiOpt.of(left, filtered);
    }

    public <X extends Exception> BiOpt<T, ?> filterLeft(ThrowingPredicate<? super T, X> predicate) throws X {
        return filterLeft(predicateOf(predicate));
    }

    /**
     * Performs predicate.test on the value of the right. The result is stored on the right.
     */
    public BiOpt<T, U> filterRight(Predicate<? super U> predicate) {
        Opt<U> filtered = right.filter(predicate);
        return BiOpt.of(left, filtered);
    }

    public <X extends Exception> BiOpt<T, U> filterRight(ThrowingPredicate<? super U, X> predicate) throws X {
        return filterRight(predicateOf(predicate));
    }

    public BiOpt<T, U> ifLeftEmpty(Supplier<T> supplier) {
        return BiOpt.of(left.ifEmpty(supplier), right);
    }

    public <X extends Exception> BiOpt<T, U> ifLeftEmpty(ThrowingSupplier<T, X> supplier) {
        return ifLeftEmpty(supplierOf(supplier));
    }

    public BiOpt<T, U> ifRightEmpty(Supplier<U> supplier) {
        return BiOpt.of(left, right.ifEmpty(supplier));
    }

    public <X extends Exception> BiOpt<T, U> ifRightEmpty(ThrowingSupplier<U, X> supplier) {
        return ifRightEmpty(supplierOf(supplier));
    }

    public Opt<T> left() {
        return left;
    }

    public <V, R> BiOpt<V, R> map(Function<T, V> leftMapper, Function<U, R> rightMapper) {
        Opt<V> newLeft = left.map(leftMapper);
        Opt<R> newRight = right.map(rightMapper);
        return of(newLeft, newRight);
    }

    public <V, R, X extends Exception> BiOpt<V, R> map(ThrowingFunction<T, V, X> leftMapper, ThrowingFunction<U, R, X> rightMapper) throws X {
        return map(functionOf(leftMapper), functionOf(rightMapper));
    }

    public <V> BiOpt<V, U> mapLeft(Function<T, V> leftMapper) {
        Opt<V> newLeft = left.map(leftMapper);
        return of(newLeft, right);
    }

    public <V, X extends Exception> BiOpt<V, U> mapLeft(ThrowingFunction<T, V, X> leftMapper) throws X {
        return mapLeft(functionOf(leftMapper));
    }

    public <R> BiOpt<T, R> mapRight(Function<U, R> rightMapper) {
        Opt<R> newRight = right.map(rightMapper);
        return of(left, newRight);
    }

    public <R, X extends Exception> BiOpt<T, R> mapRight(ThrowingFunction<U, R, X> rightMapper) throws X {
        return mapRight(functionOf(rightMapper));
    }

    /**
     * Same as matchLeft.
     */
    public <R, V> BiOpt<T, ?> match(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        return matchLeft(matchingClass, matchingMapper);
    }

    public <R, V, X extends Exception> BiOpt<T, ?> match(Class<V> matchingClass, ThrowingFunction<? super V, ? extends R, X> matchingMapper) throws X {
        return matchLeft(matchingClass, matchingMapper);
    }

    /**
     * Does a conditional mapping using matchingClass to compare to left contents. If left.getClass() == matchingClass
     * matchingMapper is used and results put into right.
     */
    public <R, V> BiOpt<T, ?> matchLeft(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        BiOpt<T, ?> match = left.match(matchingClass, matchingMapper);
        return BiOpt.of(left, match.right);
    }

    public <R, V, X extends Exception> BiOpt<T, ?> matchLeft(Class<V> matchingClass, ThrowingFunction<? super V, ? extends R, X> matchingMapper) throws X {
        return matchLeft(matchingClass, functionOf(matchingMapper));
    }

    /**
     * Matches the biOpt.left with matchingClass and performs matchingConsumer.accept if left value class and contents
     * match.
     */
    public <V> BiOpt<T, U> matchLeftValue(V matchingValue, Consumer<V> matchingConsumer) {
        left.matchValue(matchingValue, matchingConsumer);
        return this;
    }

    /**
     * Matches the class of the right value against the matchingClass, and if they are equal the
     * right side is mapped with matchingMapper and the results are put into right.
     */
    public <R, V> BiOpt<T, ?> matchRight(Class<V> matchingClass, Function<V, R> matchingMapper) {
        BiOpt<U, ?> match = right.match(matchingClass, matchingMapper);
        return BiOpt.of(this.left, match.right);
    }

    /**
     * Matches the class of the right value against the matchingClass, and if they are equal the
     * right side is mapped with matchingMapper and the results are put into right.
     */
    public <R, V, X extends Exception> BiOpt<T, ?> matchRight(Class<V> matchingClass, ThrowingFunction<V, R, X> matchingMapper) throws X {
        return matchRight(matchingClass, functionOf(matchingMapper));
    }

    public <V> BiOpt<T, U> matchRightValue(V matchingValue, Consumer<V> matchingConsumer) {
        right.matchValue(matchingValue, matchingConsumer);
        return this;
    }

    public Opt<U> right() {
        return right;
    }

}
