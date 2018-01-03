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
     * Same as matchLeft. Does a conditional U to R mapping from BiOpt<T, U> to BiOpt<T, R>.
     *
     * @param matchingClass the class to compare to.
     * @param matchingMapper the mapper to use if comparison was successful
     * @return a new BiOpt<T, R> where the right side now contains the mapped result or null if no match is found.
     */
    public <R, V> BiOpt<T, R> match(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        return matchLeft(matchingClass, matchingMapper);
    }

    /**
     * Does a conditional mapping using matchingClass to compare to left side contents. If left.getClass() == matchingClass
     * matchingMapper is used and right will contain the results.
     *
     * @param matchingClass the class to compare to
     * @param matchingMapper the mapper that will transform left side contents V into R.
     * @return a new BiOpt<T, R> with left containing original left value and right containing the mapped value. If no
     * match is found right will contain null/empty.
     */
    public <R, V> BiOpt<T, R> matchLeft(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        Validator.validateNotNull("matchingClass", matchingClass);
        Validator.validateNotNull("matchingMapper", matchingMapper);

        if (left.isPresent() &&
            !right.isPresent() &&
            matchingClass.isAssignableFrom(left.get().getClass())) {

            @SuppressWarnings("unchecked")
            V matchedValue = (V) left.get();

            Opt<R> newRight = Opt.of(matchedValue)
                                 .map(matchingMapper);

            return BiOpt.of(left, newRight);

        }

        return BiOpt.of(left, null);
    }
}
