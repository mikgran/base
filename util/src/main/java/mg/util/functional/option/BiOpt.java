package mg.util.functional.option;

import static mg.util.functional.consumer.ThrowingConsumer.consumerOf;
import static mg.util.functional.function.ThrowingFunction.functionOf;
import static mg.util.functional.predicate.ThrowingPredicate.predicateOf;
import static mg.util.functional.supplier.ThrowingSupplier.supplierOf;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import mg.util.ToStringBuilder;
import mg.util.functional.consumer.ThrowingConsumer;
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
     * Performs predicate.test on the value of the left. The result is stored to the right.
     * The left remains unchanged.
     */
    public BiOpt<T, ?> filter(Predicate<? super T> predicate) {
        return filterLeft(predicate);
    }

    /**
     * Performs predicate.test on the value of the left. The result is stored to the right.
     * The left remains unchanged.
     */
    public <X extends Exception> BiOpt<T, ?> filter(ThrowingPredicate<? super T, X> predicate) throws X {
        return filterLeft(predicateOf(predicate));
    }

    /**
     * Performs predicate.test on the value of the left. The result is stored to the right.
     * The left remains unchanged.
     */
    public BiOpt<T, ?> filterLeft(Predicate<? super T> predicate) {
        Opt<T> filtered = left.filter(predicate);
        return of(left, filtered);
    }

    /**
     * Performs predicate.test on the value of the left. The result is stored to the right.
     * The left remains unchanged.
     */
    public <X extends Exception> BiOpt<T, ?> filterLeft(ThrowingPredicate<? super T, X> predicate) throws X {
        return filterLeft(predicateOf(predicate));
    }

    /**
     * Performs predicate.test on the value of the right. The result is stored to the right. The left remains
     * unchanged.
     */
    public BiOpt<T, U> filterRight(Predicate<? super U> predicate) {
        Opt<U> filtered = right.filter(predicate);
        return of(left, filtered);
    }

    /**
     * Performs predicate.test on the value of the right. The result is stored to the right. The left remains
     * unchanged.
     */
    public <X extends Exception> BiOpt<T, U> filterRight(ThrowingPredicate<? super U, X> predicate) throws X {
        return filterRight(predicateOf(predicate));
    }

    /**
     * Performs supplier.get if left is empty. The right remains unchanged and the supplied value is stored
     * in the left.
     */
    public BiOpt<T, U> ifLeftEmpty(Supplier<T> supplier) {
        return of(left.ifEmpty(supplier), right);
    }

    /**
     * Performs supplier.get if left is empty. The right remains unchanged and the supplied value is stored
     * in the left.
     */
    public <X extends Exception> BiOpt<T, U> ifLeftEmpty(ThrowingSupplier<T, X> supplier) {
        return ifLeftEmpty(supplierOf(supplier));
    }

    /**
     * Performs supplier.get if right is empty. The left remains unchanged and the supplied value is stored
     * in the right.
     */
    public BiOpt<T, U> ifRightEmpty(Supplier<U> supplier) {
        return of(left, right.ifEmpty(supplier));
    }

    /**
     * Performs supplier.get if right is empty. The left remains unchanged and the supplied value is stored
     * in the right.
     */
    public <X extends Exception> BiOpt<T, U> ifRightEmpty(ThrowingSupplier<U, X> supplier) {
        return ifRightEmpty(supplierOf(supplier));
    }

    /**
     * Returns the left.
     */
    public Opt<T> left() {
        return left;
    }

    /**
     * Transforms the left from T to V using the leftMapper and the right from U to R using the rightMapper.
     */
    public <V, R> BiOpt<V, R> map(Function<? super T, ? extends V> leftMapper,
        Function<? super U, ? extends R> rightMapper) {

        Opt<V> newLeft = left.map(leftMapper);
        Opt<R> newRight = right.map(rightMapper);
        return of(newLeft, newRight);
    }

    /**
     * Transforms the left from T to V using the leftMapper and the right from U to R using the rightMapper.
     */
    public <V, R, X extends Exception> BiOpt<V, R> map(ThrowingFunction<? super T, ? extends V, X> leftMapper,
        ThrowingFunction<? super U, ? extends R, X> rightMapper) throws X {

        return map(functionOf(leftMapper), functionOf(rightMapper));
    }

    /**
     * Transforms the left from T to V using leftMapper. The right remains unchanged.
     */
    public <V> BiOpt<V, U> mapLeft(Function<? super T, ? extends V> leftMapper) {
        Opt<V> newLeft = left.map(leftMapper);
        return of(newLeft, right);
    }

    /**
     * Transforms the left from T to V using leftMapper. The right remains unchanged.
     */
    public <V, X extends Exception> BiOpt<V, U> mapLeft(ThrowingFunction<? super T, ? extends V, X> leftMapper) throws X {
        return mapLeft(functionOf(leftMapper));
    }

    /**
     * Transforms the right from T to V using rightMapper. The left remains unchanged.
     */
    public <R> BiOpt<T, R> mapRight(Function<? super U, ? extends R> rightMapper) {
        Opt<R> newRight = right.map(rightMapper);
        return of(left, newRight);
    }

    /**
     * Transforms the right from T to V using rightMapper. The left remains unchanged.
     */
    public <R, X extends Exception> BiOpt<T, R> mapRight(ThrowingFunction<? super U, ? extends R, X> rightMapper) throws X {
        return mapRight(functionOf(rightMapper));
    }

    /**
     * Performs a conditional mapping. If left.getClass() == matchingClass the matchingMapper is applied to the left
     * and the result is stored in a new BiOpt.right. If no match is found right remains unchanged.
     * The left always remains unchanged.
     */
    public <R, V> BiOpt<T, ?> match(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        return matchLeft(matchingClass, matchingMapper);
    }

    /**
     * Performs a conditional mapping. If left.getClass() == matchingClass the matchingMapper is applied to the left
     * and the result is stored in a new BiOpt.right. If no match is found right remains unchanged.
     * The left always remains unchanged.
     */
    public <R, V, X extends Exception> BiOpt<T, ?> match(Class<V> matchingClass, ThrowingFunction<? super V, ? extends R, X> matchingMapper) throws X {
        return matchLeft(matchingClass, matchingMapper);
    }

    /**
     * Performs a conditional mapping. If left.getClass() == matchingClass the matchingMapper is applied to the left
     * and the result is stored in a new BiOpt.right. If no match is found right remains unchanged.
     * The left always remains unchanged.
     */
    public <R, V> BiOpt<T, ?> matchLeft(Class<V> matchingClass, Function<? super V, ? extends R> matchingMapper) {
        BiOpt<T, ?> newRight = left.match(matchingClass, matchingMapper);
        if (newRight.right.isPresent()) {
            of(left, newRight.right);
        }
        return this;
    }

    /**
     * Performs a conditional mapping. If left.getClass() == matchingClass the matchingMapper is applied to the left
     * and the result is stored in a new BiOpt.right. If no match is found right remains unchanged.
     * The left always remains unchanged.
     */
    public <R, V, X extends Exception> BiOpt<T, ?> matchLeft(Class<V> matchingClass, ThrowingFunction<? super V, ? extends R, X> matchingMapper) throws X {
        return matchLeft(matchingClass, functionOf(matchingMapper));
    }

    /**
     * Matches the biOpt.left with matchingValue and performs matchingConsumer.accept(left) if left value class
     * and contents match.
     */
    public <V> BiOpt<T, U> matchLeftValue(V matchingValue, Consumer<V> matchingConsumer) {
        left.matchValue(matchingValue, matchingConsumer);
        return this;
    }

    /**
     * Matches the biOpt.left with matchingValue and performs matchingConsumer.accept(left) if left value class
     * and contents match.
     */
    public <V, X extends Exception> BiOpt<T, U> matchLeftValue(V matchingValue, ThrowingConsumer<V, X> matchingConsumer) throws X {
        matchLeftValue(matchingValue, consumerOf(matchingConsumer));
        return this;
    }

    /**
     * Performs a pattern match. If left.get().getClass() == typeRef and the predicate returns true
     * the matchingMapper is applied and the result is stored in a new BiOpt.right.
     * If no match is found or the predicate returns false the right remains unchanged.
     * The left always remains unchanged.
     */
    public <V, R> BiOpt<T, ?> matchPattern(V typeRef,
        Predicate<V> predicate,
        Function<V, R> matchingMapper) {

        return matchPatternLeft(typeRef, predicate, matchingMapper);
    }

    /**
     * Performs a pattern match. If left.get().getClass() == typeRef and the predicate returns true
     * the matchingMapper is applied and the result is stored in a new BiOpt.right.
     * If no match is found or the predicate returns false the right remains unchanged.
     * The left always remains unchanged.
     */
    public <V, R, X extends Exception> BiOpt<T, ?> matchPattern(V typeRef,
        ThrowingPredicate<V, X> predicate,
        ThrowingFunction<V, R, X> matchingMapper) throws X {

        return matchPatternLeft(typeRef, predicate, matchingMapper);
    }

    /**
     * Performs a pattern match. If left.get().getClass() == typeRef and the predicate returns true
     * the matchingMapper is applied and the result is stored in a new BiOpt.right.
     * If no match is found or the predicate returns false the right remains unchanged.
     * The left always remains unchanged.
     */
    public <V, R> BiOpt<T, ?> matchPatternLeft(V typeRef,
        Predicate<V> predicate,
        Function<V, R> matchingMapper) {

        BiOpt<T, ?> newRight = left.matchPattern(typeRef, predicate, matchingMapper);
        if (newRight.right.isPresent()) {
            return of(left, newRight.right);
        }
        return this;
    }

    /**
     * Performs a pattern match. If right.get().getClass() == typeRef and the predicate returns true
     * the matchingMapper is applied and the result is stored in a new BiOpt.right.
     * If no match is found or the predicate returns false the right remains unchanged.
     * The left always remains unchanged.
     */
    public <V, R> BiOpt<T, ?> matchPatternRight(V typeRef,
        Predicate<V> predicate,
        Function<V, R> matchingMapper) {

        BiOpt<U, ?> newRight = right.matchPattern(typeRef, predicate, matchingMapper);
        if (newRight.right.isPresent()) {
            return of(left, newRight.right);
        }
        return this;
    }

    /**
     * Matches the class of the right value against the matchingClass, and if they are equal the
     * right side is mapped with matchingMapper and the results are put into the right. If they aren't
     * equal right remains unchanged. Left always remains unchanged.
     */
    public <R, V> BiOpt<T, ?> matchRight(Class<V> matchingClass, Function<V, R> matchingMapper) {
        BiOpt<U, ?> newRight = right.match(matchingClass, matchingMapper);
        if (newRight.right.isPresent()) {
            return of(left, newRight.right);
        }
        return this;
    }

    /**
     * Matches the class of the right value against the matchingClass, and if they are equal the
     * right side is mapped with matchingMapper and the results are put into the right. If they aren't
     * equal right remains unchanged. Left always remains unchanged.
     */
    public <R, V, X extends Exception> BiOpt<T, ?> matchRight(Class<V> matchingClass,
        ThrowingFunction<V, R, X> matchingMapper) throws X {

        return matchRight(matchingClass, functionOf(matchingMapper));
    }

    /**
     * Matches the biOpt.right with matchingValue and performs matchingConsumer.accept(right) if right value class
     * and contents match.
     */
    public <V> BiOpt<T, U> matchRightValue(V matchingValue, Consumer<V> matchingConsumer) {
        right.matchValue(matchingValue, matchingConsumer);
        return this;
    }

    /**
     * Returns the right.
     */
    public Opt<U> right() {
        return right;
    }

    @Override
    public String toString() {
        return ToStringBuilder.of(this)
                              .add(t -> "" + t.left.get())
                              .add(t -> "" + t.right.get())
                              .build();
    }
}
