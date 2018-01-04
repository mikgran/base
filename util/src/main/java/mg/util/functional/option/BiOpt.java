package mg.util.functional.option;

import java.util.function.Function;

import mg.util.validation.Validator;

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
        this.left = null;
        this.right = null;
    }

    private BiOpt(Opt<T> left, Opt<U> right) {
        this.left = left;
        this.right = right;
    }

    private BiOpt(T left, U right) {
        this.left = Opt.of(left);
        this.right = Opt.of(right);
    }

    public Opt<T> getLeft() {
        return left;
    }

    public Opt<U> getRight() {
        return right;
    }

    public <V, R> BiOpt<V, R> map(Function<T, V> leftMapper, Function<U, R> rightMapper) {

        Opt<V> newLeft = left.map(leftMapper);
        Opt<R> newRight = right.map(rightMapper);
        return of(newLeft, newRight);
    }

    public <V> BiOpt<V, U> mapLeft(Function<T, V> leftMapper) {

        Opt<V> newLeft = left.map(leftMapper);
        return of(newLeft, right);
    }

    public <R> BiOpt<T, R> mapRight(Function<U, R> rightMapper) {

        Opt<R> newRight = right.map(rightMapper);
        return of(left, newRight);
    }

    /**
     * Same as matchLeft.
     */
    public <R, V> BiOpt<T, ?> match(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        return matchLeft(matchingClass, matchingMapper);
    }

    /**
     * Does a conditional mapping using matchingClass to compare to left contents. If left.getClass() == matchingClass
     * matchingMapper is used and results put into right.
     */
    public <R, V> BiOpt<T, ?> matchLeft(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        Validator.validateNotNull("matchingClass", matchingClass);
        Validator.validateNotNull("matchingMapper", matchingMapper);

        if (left.isPresent() &&
            matchingClass.isAssignableFrom(left.get().getClass())) {

            @SuppressWarnings("unchecked")
            V matchedValue = (V) left.get();

            Opt<R> newRight = Opt.of(matchedValue)
                                 .map(matchingMapper);

            return BiOpt.of(left, newRight);
        }

        return this;
    }

    /**
     * Matches the class of the right value against the matchingClass, and if they are equal the
     * right side is mapped with matchingMapper and the results are put into right.
     */
    public <R, V> BiOpt<T, ?> matchRight(Class<V> matchingClass, Function<V, R> matchingMapper) {
        Validator.validateNotNull("matchingClass", matchingClass);
        Validator.validateNotNull("matchingMapper", matchingMapper);

        if (right.isPresent() &&
            matchingClass.isAssignableFrom(right.get().getClass())) {

            @SuppressWarnings("unchecked")
            V matchedValue = (V) right.get();

            Opt<R> newRight = Opt.of(matchedValue)
                                 .map(matchingMapper);

            return BiOpt.of(left, newRight);
        }

        return this;
    }
}
