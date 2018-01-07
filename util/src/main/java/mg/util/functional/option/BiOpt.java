package mg.util.functional.option;

import static mg.util.functional.function.ThrowingFunction.functionOf;

import java.util.function.Consumer;
import java.util.function.Function;

import mg.util.functional.function.ThrowingFunction;

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
        left.match(matchingClass, matchingMapper);
        return this;
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
        right.match(matchingClass, matchingMapper);
        return this;
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
