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
import mg.util.validation.Validator;

/**
 * A class for holding Opt.match* results.
 *
 * Typically all methods should return a new BiOpt Where the BiOpt.left always contains the
 * original value matched against and the BiOpt.right contains the match* result. The exceptions are
 * the mapping methods that map left or right contents directly.
 *
 * NOTE: the class is not symmetrical in the sense that match
 * methods use BiOpt.right for transformation results.<br /><br />
 *
 * For instance typical BiOpt.of("", "").match("", s -> s.length() == 0, s -> "string length was zero")
 * usage ends up with BiOpt.of("", "string length was zero").
 */
// TOIMPROVE: test coverage.
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
     * Performs a predicate test on the value of the left.
     *
     * @param predicate the predicate to test.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt for the original left and the filtered result.
     * <pre>
     * Returned values:
     * Is left   Does predicate  Result:
     * present?  test true?
     * yes       yes             BiOpt(left, filteredLeft)
     * yes       no              BiOpt(left, empty)
     * no        *               BiOpt(left, empty)
     * * == yes or no
     * </pre>
     */
    public BiOpt<T, ?> filter(Predicate<? super T> predicate) {
        return filterLeft(predicate);
    }

    /**
     * Performs a predicate test on the value of the left.
     *
     * @param predicate the predicate to test.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt for the original left and the filtered result.
     * @throws X The filter is assumed to be able to throw an Exception.
     * <pre>
     * Returned values:
     * Is left   Does predicate  Result:
     * present?  test true?
     * yes       yes             BiOpt(left, filteredLeft)
     * yes       no              BiOpt(left, empty)
     * no        *               BiOpt(left, empty)
     * * == yes or no
     * </pre>
     */
    public <X extends Exception> BiOpt<T, ?> filter(ThrowingPredicate<? super T, X> predicate) throws X {
        return filterLeft(predicateOf(predicate));
    }

    /**
     * Performs a predicate test on the value of the right.
     *
     * @param predicate the predicate to test.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt for the original right and the filtered result.
     * <pre>
     * Returned values:
     * Is right  Does predicate  Result:
     * present?  test true?
     * yes       yes             BiOpt(left, right)
     * yes       no              BiOpt(left, empty)
     * no        *               BiOpt(left, empty)
     * * == yes or no
     * </pre>
     */
    public BiOpt<T, U> filterRight(Predicate<? super U> predicate) {
        Opt<U> filtered = right.filter(predicate);
        return of(left, filtered);
    }

    /**
     * Performs a predicate test on the value of the right.
     *
     * @param predicate the predicate to test.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt for the original right and the filtered result.
     * @throws X The filter is assumed to be able to throw an Exception.
     * <pre>
     * Returned values:
     * Is right  Does predicate  Result:
     * present?  test true?
     * yes       yes             BiOpt(left, right)
     * yes       no              BiOpt(left, empty)
     * no        *               BiOpt(left, empty)
     * * == yes or no
     * </pre>
     */
    public <X extends Exception> BiOpt<T, U> filterRight(ThrowingPredicate<? super U, X> predicate) throws X {
        return filterRight(predicateOf(predicate));
    }

    /**
     * Performs supplier.get if left is empty.
     *
     * @param supplier The supplier to use if left is empty.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt of left or supplied new left and right.
     * <pre>
     * Returned values:
     * Is left   Result:
     * present?
     * yes       BiOpt(left, right)
     * no        BiOpt(suppliedLeft, right)
     * </pre>
     */
    public BiOpt<T, U> ifLeftEmpty(Supplier<T> supplier) {
        return of(left.ifEmpty(supplier), right);
    }

    /**
     * Performs supplier.get if left is empty.
     *
     * @param supplier The supplier to use if left.ifEmpty == true.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt of left or supplied new left and right.
     * @throws X The supplier is assumed to be able to throw an Exception.
     * <pre>
     * Returned values:
     * Is left   Result:
     * present?
     * yes       BiOpt(left, right)
     * no        BiOpt(suppliedLeft, right)
     * </pre>
     */
    public <X extends Exception> BiOpt<T, U> ifLeftEmpty(ThrowingSupplier<T, X> supplier) {
        return ifLeftEmpty(supplierOf(supplier));
    }

    /**
     * Performs supplier.get if right is empty.
     *
     * @param supplier The supplier to use if right.ifEmpty == true.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt of left and right or supplied new right.
     * <pre>
     * Returned values:
     * Is right  Result:
     * present?
     * yes       BiOpt(left, right)
     * no        BiOpt(left, suppliedRight)
     * </pre>
     */
    public BiOpt<T, U> ifRightEmpty(Supplier<U> supplier) {
        return of(left, right.ifEmpty(supplier));
    }

    /**
     * Performs supplier.get if right is empty.
     *
     * @param supplier The supplier to use if right.ifEmpty == true.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt of left and right or supplied new right.
     * @throws X The supplier is assumed to be able to throw an Exception.
     * <pre>
     * Returned values:
     * Is right  Result:
     * present?
     * yes       BiOpt(left, right)
     * no        BiOpt(left, suppliedRight)
     * </pre>
     */
    public <X extends Exception> BiOpt<T, U> ifRightEmpty(ThrowingSupplier<U, X> supplier) {
        return ifRightEmpty(supplierOf(supplier));
    }

    /**
     * Returns the left Opt of type T.
     *
     * @return Returns the left of this BiOpt.
     */
    public Opt<T> left() {
        return left;
    }

    /**
     * Transforms the left from T to V.
     *
     * @param leftMapper The T to V transforming function to apply if the left is present.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt of the original left and transformed left.
     *
     * <pre>
     * Is value  Returned BiOpt:
     * present?
     * yes       BiOpt(left, transformedLeft**)
     * no        BiOpt(left, empty)
     * ** == may be null.
     * </pre>
     */
    public <V> BiOpt<V, U> map(Function<? super T, ? extends V> leftMapper) {
        Opt<V> newLeft = left.map(leftMapper);
        return of(newLeft, right);
    }

    /**
     * Transforms the left from T to V and the right from U to R.
     *
     * @param leftMapper The T to V transforming function to apply if the left is present.
     * If not present an Opt.empty is returned.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @param rightMapper The U to R transforming function to apply if the right is present.
     * If not present an Opt.empty is returned.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt which contains the results of the mapping.
     *
     * <pre>
     * Is left   Is right  Returned BiOpt:
     * present?  present?
     * yes       yes       BiOpt(transformedLeft**, transformedRight**)
     * yes       no        BiOpt(transformedLeft**, empty)
     * no        yes       BiOpt(empty, transformedRight**)
     * ** == may be null.
     * </pre>
     */
    public <V, R> BiOpt<V, R> map(Function<? super T, ? extends V> leftMapper,
        Function<? super U, ? extends R> rightMapper) {

        Opt<V> newLeft = left.map(leftMapper);
        Opt<R> newRight = right.map(rightMapper);
        return of(newLeft, newRight);
    }

    /**
     * Transforms the left from T to V.
     *
     * @param leftMapper The T to V transforming function to apply if the left is present.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt of the original left and transformed left.
     * @throws X The leftMapper is assumed to be able to throw an Exception.
     *
     * <pre>
     * Is value  Returned BiOpt:
     * present?
     * yes       BiOpt(left, transformedLeft**)
     * no        BiOpt(left, empty)
     * ** == may be null.
     * </pre>
     */
    public <V, X extends Exception> BiOpt<V, U> map(ThrowingFunction<? super T, ? extends V, X> leftMapper) throws X {
        return map(functionOf(leftMapper));
    }

    /**
     * Transforms the left from T to V and the right from U to R.
     *
     * @param leftMapper The T to V transforming function to apply if the left is present.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @param rightMapper The U to R transforming function to apply if the right is present.
     * <br />An {@link IllegalArgumentException} is thrown if null.
     * @return Returns a new BiOpt of transformed left and transformed right.
     * @throws X The transforming functions are assumed to be able to throw an Exception.
     */
    public <V, R, X extends Exception> BiOpt<V, R> map(ThrowingFunction<? super T, ? extends V, X> leftMapper,
        ThrowingFunction<? super U, ? extends R, X> rightMapper) throws X {

        return map(functionOf(leftMapper), functionOf(rightMapper));
    }

    /**
     * Transforms the value from U to R.
     *
     * @param rightMapper The mapper to use for the transformation of the right.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt of left and the transformed right.
     *
     * <pre>
     * Is value  Returned BiOpt:
     * present?
     * yes       BiOpt(left, transformedRight**)
     * no        BiOpt(left, empty)
     * ** == may be null.
     * </pre>
     */
    public <R> BiOpt<T, R> mapRight(Function<? super U, ? extends R> rightMapper) {
        Opt<R> newRight = right.map(rightMapper);
        return of(left, newRight);
    }

    /**
     * Transforms the value from U to R.
     *
     * @param rightMapper The mapper to use for the transformation of the right.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt of left and the transformed right.
     * @throws X The rightMapper is assumbed to be able to throw an Exception.
     *
     * <pre>
     * Is value  Returned BiOpt:
     * present?
     * yes       BiOpt(left, transformedRight**)
     * no        BiOpt(left, empty)
     * ** == may be null.
     * </pre>
     */
    public <R, X extends Exception> BiOpt<T, R> mapRight(ThrowingFunction<? super U, ? extends R, X> rightMapper) throws X {
        return mapRight(functionOf(rightMapper));
    }

    /**
     * Performs a conditional mapping of the left. The class of the left value and typeRef
     * may not be the same.
     *
     * @param typeRef The class to compare against the class of the left value. If they are equal
     * the matchingMapper is applied.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in an Opt:<br />
     * <pre>
     * Is value  Does typeRef  Is matching     Returned BiOpt:
     * present?  match?        mapper applied?
     * yes       yes           yes             BiOpt(left, transformedLeft**)
     * yes       yes           no              BiOpt(left, empty)
     * yes       no            no              BiOpt(left, empty)
     * no        *             no              BiOpt(left, empty)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <R, V> BiOpt<T, ?> match(Class<V> typeRef, Function<? super V, ? extends R> matchingMapper) {
        return matchLeft(typeRef, matchingMapper);
    }

    /**
     * Performs a conditional mapping of the left. The class of the left value and typeRef
     * may not be the same.
     *
     * @param typeRef The class to compare against the class of the left value. If they are equal
     * the matchingMapper is applied.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in an Opt:<br />
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does typeRef  Is matching     Returned BiOpt:
     * present?  match?        mapper applied?
     * yes       yes           yes             BiOpt(left, transformedLeft**)
     * yes       yes           no              BiOpt(left, empty)
     * yes       no            no              BiOpt(left, empty)
     * no        *             no              BiOpt(left, empty)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <R, V, X extends Exception> BiOpt<T, ?> match(Class<V> matchingClass,
        ThrowingFunction<? super V, ? extends R, X> matchingMapper) throws X {

        return matchLeft(matchingClass, functionOf(matchingMapper));
    }

    /**
     * Performs a conditional consuming of the left. The class of the matchingValue and
     * the class of the left value may not be the same.
     *
     * @param matchingValue The class and value to compare against the class and value of the left value.
     * If they are equal the matchingMapper is applied.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in an Opt:<br />
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does matching  Is consumer
     * present?  value match?   applied?
     * yes       yes            yes
     * yes       no             no
     * no        *              no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V> BiOpt<T, U> match(V matchingValue,
        Consumer<V> matchingConsumer) {

        matchLeftValue(matchingValue, matchingConsumer);
        return this;
    }

    /**
     * Performs a conditional consuming based on pattern match. If left is present,
     * typeRef.getClass == left.getClass and predicate tests true a matchingConsumer.accept
     * is performed on the value of the left.
     *
     * @param typeRef The typeRef class to compare the value of the left class against.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingConsumer The matchingConsumer to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns this.
     *
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching
     * present?  match?       match?          consumer applied?
     * yes       yes          yes             yes
     * yes       yes          no              no
     * yes       no           *               no
     * no        *            *               no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V> BiOpt<T, U> match(V typeRef,
        Predicate<V> predicate,
        Consumer<V> matchingConsumer) {

        Validator.validateNotNull("typeRef", typeRef);
        Validator.validateNotNull("predicate", predicate);
        Validator.validateNotNull("matchingConsumer", matchingConsumer);

        if (left.isPresent() &&
            left.get().getClass().isAssignableFrom(typeRef.getClass())) {

            @SuppressWarnings("unchecked")
            V matchedValue = (V) left.get();

            if (predicate.test(matchedValue)) {

                matchingConsumer.accept(matchedValue);
            }
        }
        return this;
    }

    /**
     * Performs a conditional transformation of the left using a pattern match.
     * The types of left value and typeRef may be different. If they match, the predicate is tested,
     * and finally the matchingMapper is applied if all conditions match.
     *
     * @param typeRef The type reference object to match against the values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingMapper is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply to.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in a new Opt.
     * The transformedValue may be null -> Opt.empty. Returns this if no match is found.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching     Returned BiOpt:
     * present?  match?       match?          mapper applied?
     * yes       yes          yes             yes             BiOpt(left, transformedLeft**)
     * yes       yes          no              no              BiOpt(left, empty)
     * yes       no           *               no              BiOpt(left, empty)
     * no        *            *               no              BiOpt(left, empty)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, R> BiOpt<T, ?> match(V typeRef,
        Predicate<V> predicate,
        Function<V, R> matchingMapper) {

        return matchPatternLeft(typeRef, predicate, matchingMapper);
    }

    /**
     * Performs a conditional consuming of the left. The class of the matchingValue and
     * the class of the left value may not be the same.
     *
     * @param matchingValue The class and value to compare against the class and value of the left value.
     * If they are equal the matchingMapper is applied.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in an Opt:<br />
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does matching  Is left value applied to consumer?
     * present?  value match?
     * yes       yes            yes
     * yes       no             no
     * no        *              no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, X extends Exception> BiOpt<T, U> match(V matchingValue,
        ThrowingConsumer<V, X> matchingConsumer) throws X {

        match(matchingValue, consumerOf(matchingConsumer));
        return this;
    }

    /**
     * Performs a conditional consuming based on pattern match. If left is present,
     * typeRef.getClass == left.getClass and predicate tests true a matchingConsumer.accept
     * is performed on the value of the left.
     *
     * @param typeRef The typeRef class to compare the value of the left class against.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingConsumer The matchingConsumer to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns this.
     * @throws The matchingConsumer is assumed to be able to throw an Exception.
     *
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching
     * present?  match?       match?          consumer applied?
     * yes       yes          yes             yes
     * yes       yes          no              no
     * yes       no           *               no
     * no        *            *               no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, X extends Exception> BiOpt<T, U> match(V typeRef,
        ThrowingPredicate<V, X> predicate,
        ThrowingConsumer<V, X> matchingConsumer) throws X {

        return match(typeRef, predicateOf(predicate), consumerOf(matchingConsumer));
    }

    /**
     * Performs a conditional transformation of the left using a pattern match.
     * The types of left value and typeRef may be different. If they match, the predicate is tested,
     * and finally the matchingMapper is applied if all conditions match.
     *
     * @param typeRef The type reference object to match against the values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingMapper is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply to.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in a new Opt.
     * The transformedValue may be null -> Opt.empty. Returns this if no match is found.
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching     Returned BiOpt:
     * present?  match?       match?          mapper applied?
     * yes       yes          yes             yes             BiOpt(left, transformedLeft**)
     * yes       yes          no              no              BiOpt(left, empty)
     * yes       no           *               no              BiOpt(left, empty)
     * no        *            *               no              BiOpt(left, empty)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, R, X extends Exception> BiOpt<T, ?> match(V typeRef,
        ThrowingPredicate<V, X> predicate,
        ThrowingFunction<V, R, X> matchingMapper) throws X {

        return matchPatternLeft(typeRef, predicate, matchingMapper);
    }

    /**
     * Performs a conditional transformation of the Right using a pattern match.
     * The types of right value and typeRef may be different. If they match, the predicate is tested,
     * and finally the matchingMapper is applied if all conditions match.
     *
     * @param typeRef The type reference object to match against the values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingMapper is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply to.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the new Opt for transformed right. If
     * no match is found, right remains the same. Due to varying types, BiOpt<T, ?> is returned.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching     Returned BiOpt:
     * present?  match?       match?          mapper applied?
     * yes       yes          yes             yes             BiOpt(left, transformedRight**)
     * yes       yes          no              no              BiOpt(left, right)
     * yes       no           *               no              BiOpt(left, right)
     * no        *            *               no              BiOpt(left, right)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <R, V> BiOpt<T, ?> matchRight(Class<V> matchingClass, Function<V, R> matchingMapper) {
        BiOpt<U, ?> newRight = right.match(matchingClass, matchingMapper);
        if (newRight.right.isPresent()) {
            return of(left, newRight.right);
        }
        return this;
    }

    /**
     * Performs a conditional transformation of the Right using a pattern match.
     * The types of right value and typeRef may be different. If they match, the predicate is tested,
     * and finally the matchingMapper is applied if all conditions match.
     *
     * @param typeRef The type reference object to match against the values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingMapper is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply to.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the new Opt for transformed right. If
     * no match is found, right remains the same. Due to varying types, BiOpt<T, ?> is returned.
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching     Returned BiOpt:
     * present?  match?       match?          mapper applied?
     * yes       yes          yes             yes             BiOpt(left, transformedRight**)
     * yes       yes          no              no              BiOpt(left, right)
     * yes       no           *               no              BiOpt(left, right)
     * no        *            *               no              BiOpt(left, right)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <R, V, X extends Exception> BiOpt<T, ?> matchRight(Class<V> matchingClass,
        ThrowingFunction<V, R, X> matchingMapper) throws X {

        return matchRight(matchingClass, functionOf(matchingMapper));
    }

    /**
     * Matches the biOpt.right with matchingValue and performs matchingConsumer.accept(right) if right value class
     * and right value match with matchingValue.
     * @param matchingValue The matching value to match against.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingConsumer The consumer to apply if all conditions are met.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns this.
     *
     * <pre>
     * Is value  Does matching Is matching
     * present?  value match?  consumer applied?
     * yes       yes           yes
     * yes       yes           no
     * yes       no            no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V> BiOpt<T, U> matchRight(V matchingValue, Consumer<V> matchingConsumer) {
        right.match(matchingValue, matchingConsumer);
        return this;
    }

    /**
     * Performs a conditional consuming of the right using a pattern match. If the type if the right
     * and the predicate tests true the matchingConsumer is applied.
     *
     * @param typeRef The type reference object to match against the right values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingConsumer is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingConsumer The consumer to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns this.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching
     * present?  match?       test true?      consumer applied?
     * yes       yes          yes             yes
     * yes       yes          no              no
     * yes       no           *               no
     * no        *            *               no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V> BiOpt<T, U> matchRight(V typeRef,
        Predicate<V> predicate,
        Consumer<V> matchingConsumer) {

        Validator.validateNotNull("typeRef", typeRef);
        Validator.validateNotNull("predicate", predicate);
        Validator.validateNotNull("matchingConsumer", matchingConsumer);

        if (right.isPresent() &&
            right.get().getClass().isAssignableFrom(typeRef.getClass())) {

            @SuppressWarnings("unchecked")
            V matchedValue = (V) right.get();

            if (predicate.test(matchedValue)) {

                matchingConsumer.accept(matchedValue);
            }
        }
        return this;
    }

    /**
     * Performs a conditional transformation of the right using a pattern match.
     * The types of right value and typeRef may be different. If they match, the predicate is tested,
     * and finally the matchingMapper is applied if all conditions match.
     *
     * @param typeRef The type reference object to match against the values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingMapper is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply to.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in a new Opt.
     * The transformedValue may be null -> Opt.empty. Returns this if no match is found.
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching     Returned BiOpt:
     * present?  match?       match?          mapper applied?
     * yes       yes          yes             yes             BiOpt(left, transformedRight**)
     * yes       yes          no              no              BiOpt(left, right)
     * yes       no           *               no              BiOpt(left, right)
     * no        *            *               no              BiOpt(left, right)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, R> BiOpt<T, ?> matchRight(V typeRef,
        Predicate<V> predicate,
        Function<V, R> matchingMapper) {

        BiOpt<U, ?> newRight = right.match(typeRef, predicate, matchingMapper);
        if (newRight.right.isPresent()) {
            return of(left, newRight.right);
        }
        return this;
    }

    /**
     * Matches the biOpt.right with matchingValue and performs matchingConsumer.accept(right) if right value class
     * and right value match with matchingValue.
     *
     * @param matchingValue The matching value to match against.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingConsumer The consumer to apply if all conditions are met.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns this.
     * @throws X The matchingConsumer is assumed to be able to throw an Exception.
     *
     * <pre>
     * Is value  Does matching Is matching
     * present?  value match?  consumer applied?
     * yes       yes           yes
     * yes       yes           no
     * yes       no            no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, X extends Exception> BiOpt<T, U> matchRight(V matchingValue, ThrowingConsumer<V, X> matchingConsumer) throws X {
        return matchRight(matchingValue, consumerOf(matchingConsumer));
    }

    /**
     * Performs a conditional consuming of the right using a pattern match. If the type if the right
     * and the predicate tests true the matchingConsumer is applied.
     *
     * @param typeRef The type reference object to match against the right values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingConsumer is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingConsumer The consumer to apply.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns this.
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching
     * present?  match?       test true?      consumer applied?
     * yes       yes          yes             yes
     * yes       yes          no              no
     * yes       no           *               no
     * no        *            *               no
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, X extends Exception> BiOpt<T, U> matchRight(V typeRef,
        ThrowingPredicate<V, X> predicate,
        ThrowingConsumer<V, X> matchingConsumer) throws X {

        return matchRight(typeRef, predicateOf(predicate), consumerOf(matchingConsumer));
    }

    /**
     * Performs a conditional transformation of the right using a pattern match.
     * The types of right value and typeRef may be different. If they match, the predicate is tested,
     * and finally the matchingMapper is applied if all conditions match.
     *
     * @param typeRef The type reference object to match against the values class.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingMapper is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply to.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in a new Opt.
     * The transformedValue may be null -> Opt.empty. Returns this if no match is found.
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     * <pre>
     * Is value  Does typeRef Does predicate  Is matching     Returned BiOpt:
     * present?  match?       match?          mapper applied?
     * yes       yes          yes             yes             BiOpt(left, transformedRight**)
     * yes       yes          no              no              BiOpt(left, right)
     * yes       no           *               no              BiOpt(left, right)
     * no        *            *               no              BiOpt(left, right)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, R, X extends Exception> BiOpt<T, ?> matchRight(V typeRef,
        ThrowingPredicate<V, X> predicate,
        ThrowingFunction<V, R, X> matchingMapper) throws X {

        return matchRight(typeRef, predicateOf(predicate), functionOf(matchingMapper));
    }

    /**
     * Returns the right.
     * @return Returns the right of this BiOpt.
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

    private BiOpt<T, ?> filterLeft(Predicate<? super T> predicate) {
        Opt<T> filtered = left.filter(predicate);
        return of(left, filtered);
    }

    private <R, V> BiOpt<T, ?> matchLeft(Class<V> matchingClass,
        Function<? super V, ? extends R> matchingMapper) {

        BiOpt<T, ?> newRight = left.match(matchingClass, matchingMapper);
        if (newRight.right.isPresent()) {
            of(left, newRight.right);
        }
        return this;
    }

    private <V> BiOpt<T, U> matchLeftValue(V matchingValue, Consumer<V> matchingConsumer) {
        left.match(matchingValue, matchingConsumer);
        return this;
    }

    private <V, R> BiOpt<T, ?> matchPatternLeft(V typeRef,
        Predicate<V> predicate,
        Function<V, R> matchingMapper) {

        BiOpt<T, ?> newRight = left.match(typeRef, predicate, matchingMapper);
        if (newRight.right.isPresent()) {
            return of(left, newRight.right);
        }
        return this;
    }
}
