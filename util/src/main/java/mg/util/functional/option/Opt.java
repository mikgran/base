package mg.util.functional.option;

import static mg.util.functional.consumer.ThrowingConsumer.consumerOf;
import static mg.util.functional.function.ThrowingFunction.functionOf;
import static mg.util.functional.predicate.ThrowingPredicate.predicateOf;
import static mg.util.functional.supplier.ThrowingSupplier.supplierOf;

import java.util.Objects;
import java.util.Optional;
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
 * A value container class for dealing with the non-null paradigm, to promote
 * the use of functional programming approach and to help dealing with the absence of
 * a value by offering tools to alter the program flow without the need to
 * continuously write code to inspect whether the value is present or not.<br /><br />
 *
 * The equals of this class is performed on the contents: Opt.of("value").equals(Of.of("value")) == true.
 *
 * All filtering, mapping, and matching methods are content based. Methods are executed if isPresent()
 * evaluates true. All methods return the original or transformed value in a new Opt.of(transformedValue) or
 * BiOpt.of(originalValue, transformedValue) for easy chaining of methods.<br /><br />
 *
 * If any construction, transformation or filtering method results in null value the Opt.empty() is returned. <br /><br />
 *
 * <pre>
 * For instance, the following call chain executes fully:
 *      Opt.of("value1")
 *         .map(s -> s + "2")
 *         .get()
 *
 * The call chain is equal to:
 *
 *      Opt.of("value2")
 *         .get()
 *
 * While the following call chain returns Opt.empty() at the second call, and the
 * call of the map is skipped.
 *
 *      Opt.of("anotherValue1")
 *         .filter(s -> s.length() == 5) // filter returns Opt.empty()
 *         .map(s -> s + "2")
 *         .get()
 *
 * The call chain is equal to:
 *
 *      Opt.empty() or ((T)null);
 * </pre>
 */
public class Opt<T> {

    private static final Opt<?> EMPTY = new Opt<>();
    private final T value;

    /**
     * Creates an empty value container of type T.
     *
     * @return Returns an empty value container of type T.
     */
    public static <T> Opt<T> empty() {
        @SuppressWarnings("unchecked")
        Opt<T> t = (Opt<T>) EMPTY;
        return t;
    }

    /**
     * Creates a new value container from an existing container.
     *
     * @param opt The existing value container.
     * @return Returns an Opt.empty() or a new value container with type T value.
     */
    public static <T> Opt<T> of(Opt<T> opt) {
        return opt == null || !opt.isPresent() ? empty() : new Opt<>(opt.get());
    }

    /**
     * Creates a new value container from an existing Optional.
     *
     * @param The existing value container.
     * @return Returns an Opt.empty() or a new value container with type T value.
     */
    public static <T> Opt<T> of(Optional<T> optional) {
        return optional == null || !optional.isPresent() ? empty() : new Opt<>(optional.get());
    }

    /**
     * Creates a new value container from value.
     *
     * @param The value to wrap in a new Opt value container.
     * @return Returns an Opt.empty() or a new value container with type T value.
     */
    public static <T> Opt<T> of(T value) {
        return value == null ? empty() : new Opt<>(value);
    }

    private Opt() {
        this.value = null;
    }

    private Opt(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Opt)) {
            return false;
        }

        Opt<?> other = (Opt<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Performs a test of the predicate if the value is present.
     *
     * @param predicate The predicate to test. An {@link IllegalArgumentException} is thrown if null.
     * @return If the predicate evaluates to true <code>this</code> is returned otherwise an Opt.empty is returned.
     */
    public Opt<T> filter(Predicate<? super T> predicate) {
        Validator.validateNotNull("predicate", predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    /**
     * Performs a test of the predicate if the value is present.
     *
     * @param throwingPredicate The predicate to test. An {@link IllegalArgumentException} is thrown if null.
     * @return If the predicate evaluates to true <code>this</code> is returned otherwise an Opt.empty is returned.
     * @throws X The throwingPredicate is assumed to be able to throw an Exception.
     */
    public <X extends Exception> Opt<T> filter(ThrowingPredicate<? super T, X> throwingPredicate) throws X {
        return filter(predicateOf(throwingPredicate));
    }

    /**
     * Performs a transformation of the value. If the mapper would return an Opt containing the transformed
     * value, it is not re-wrapped again in a new Opt.
     *
     * @param mapper The mapper to use on the value if it is present. If null an {@link IllegalArgumentException}
     * is thrown.
     * @return An Opt containing the transformed value or Opt.empty if the value is not present or the transformation
     * returns a null.
     */
    public <U> Opt<U> flatMap(Function<? super T, Opt<U>> mapper) {
        Validator.validateNotNull("mapper", mapper);
        if (!isPresent()) {
            return empty();
        } else {
            Opt<U> result = mapper.apply(value);
            if (result == null) {
                result = Opt.empty();
            }
            return result;
        }
    }

    /**
     * Performs a transformation of the value. If the mapper would return an Opt containing the transformed
     * value, it is not re-wrapped again in a new Opt.
     *
     * @param mapper The mapper to use on the value if it is present.
     * @return An Opt containing the transformed value or Opt.empty if the value is not present or the transformation
     * returns a null.
     * @throws X The mapper is assumed to be able to throw an Exception.
     */
    public <X extends Exception, U> Opt<U> flatMap(ThrowingFunction<? super T, Opt<U>, X> mapper) throws X {
        return flatMap(functionOf(mapper));
    }

    /**
     * Returns the contents of this value container. Returned value may be null.
     */
    public T get() {
        return value;
    }

    /**
     * Performs a T to U transformation of the value and returns the new raw value.
     *
     * @param mapper The transforming function to apply to the value if it is present.
     * @return Returns the transformed value or a null of type U.
     */
    public <U> U getAndMap(Function<? super T, ? extends U> mapper) {
        Validator.validateNotNull("mapper", mapper);
        if (isPresent()) {
            return mapper.apply(value);
        } else {
            return null;
        }
    }

    /**
     * Performs a T to U transformation of the value and returns the new raw value.
     *
     * @param mapper The transforming function to apply to the value if it is present.
     * @return Returns the transformed value or a null of type U.
     * @throws X The transforming function is assumed to be able to throw an Exception.
     */
    public <U, X extends Exception> U getAndMap(ThrowingFunction<? super T, ? extends U, X> mapper) throws X {
        return getAndMap(functionOf(mapper));
    }

    /**
     * Returns the value or other.
     *
     * @param other The alternative to return if value is null.
     * @return Returns the value or other if it is missing. The returned value may be null.
     */
    public T getOrElse(T other) {
        return value != null ? value : other;
    }

    /**
     * Returns the value or the value from the supplier.
     *
     * @param supplier The supplier to get new value from if value is null. If null an
     * {@link IllegalArgumentException} is thrown
     * @return Returns the value or a new value from supplier. The returned value may be null.
     */
    public T getOrElseGet(Supplier<? extends T> supplier) {
        Validator.validateNotNull("supplier", supplier);
        return value != null ? value : supplier.get();
    }

    /**
     * Returns the value or the value from the supplier.
     *
     * @param supplier The supplier to get new value from if value is null. If null an
     * {@link IllegalArgumentException} is thrown
     * @return Returns the value or a new value from supplier. The returned value may be null.
     * @throws X The supplier is assumed to be able to throw an Exception.
     */
    public <X extends Exception> T getOrElseGet(ThrowingSupplier<? extends T, X> supplier) throws X {
        return getOrElseGet(supplierOf(supplier));
    }

    /**
     * Returns the value or throws an Exception supplied by the exceptionSupplier.
     *
     * @param exceptionSupplier The supplier to provide the exception to Throw.
     * @return Returns this if the value is present.
     * @throws X This method throws exceptions:<br />
     * 1. if exceptionSupplier is null -> {@link IllegalArgumentException} <br />
     * 2. if exception supplied by exceptionSupplier is null -> {@link IllegalArgumentException} <br />
     * 3. if value is null -> X extends Exception <br />
     */
    public <X extends Exception> T getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value != null) {
            return value;
        } else {
            throw getExceptionOrElseThrowIAE(exceptionSupplier);
        }
    }

    /**
     * Returns the value or throws an Exception supplied by the exceptionSupplier.
     *
     * @param exceptionSupplier The supplier to provide the exception to Throw.
     * @return Returns this if the value is present.
     * @throws X This method throws exceptions:<br />
     * 1. if exceptionSupplier is null -> {@link IllegalArgumentException} <br />
     * 2. if exception supplied by exceptionSupplier is null -> {@link IllegalArgumentException} <br />
     * 3. if value is null -> X extends Exception <br />
     * 4. if during exceptionSupplier.get() an exception is thrown -> X extends Exception<br />
     */
    public <X extends Exception> T getOrElseThrow(ThrowingSupplier<X, X> exceptionSupplier) throws X {
        return getOrElseThrow(supplierOf(exceptionSupplier));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Gets a new value from supplier for this value container.
     *
     * @param supplier the supplier to use to get a new value. If null {@link IllegalArgumentException} is thrown.
     * @return a new Opt containing the supplied value if this.value is null.
     */
    public Opt<T> ifEmpty(Supplier<T> supplier) {
        Validator.validateNotNull("supplier", supplier);
        if (value == null) {
            return Opt.of(supplier.get());
        }
        return this;
    }

    /**
     * Gets a new value from supplier for this value container.
     *
     * @param throwingSupplier the supplier to use to get a new value. If null {@link IllegalArgumentException} is thrown.
     * @return a new Opt containing the supplied value if this.value is null.
     * @throws X the supplier is assumed to be able to thrown an Exception.
     */
    public <X extends Exception> Opt<T> ifEmpty(ThrowingSupplier<T, X> throwingSupplier) throws X {
        return ifEmpty(supplierOf(throwingSupplier));
    }

    /**
     * Throws an exception provided by the exceptionSupplier if no value is present.
     *
     * @param exceptionSupplier The supplier to get the exception to be thrown. If null an {@link IllegalArgumentException}
     * is thrown.
     * @return Returns this.
     * @throws X Throws an Exception if value is null.
     */
    public <X extends Exception> Opt<T> ifEmptyThrow(Supplier<X> exceptionSupplier) throws X {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value == null) {
            throw getExceptionOrElseThrowIAE(exceptionSupplier);
        }
        return this;
    }

    /**
     * Performs consumer.accept(value) if value is present.
     *
     * @param consumer The consumer to pass the value to. If null an {@link IllegalArgumentException}
     * is thrown.
     * @return Returns this.
     */
    public Opt<T> ifPresent(Consumer<? super T> consumer) {
        Validator.validateNotNull("consumer", consumer);
        if (value != null) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Performs consumer.accept(value) if value is present.
     *
     * @param consumer The consumer to pass the value to. If null an {@link IllegalArgumentException}
     * is thrown.
     * @return Returns this.
     * @throws X The throwingConsumer is assumed to be able to throw an Exception.
     */
    public <X extends Exception> Opt<T> ifPresent(ThrowingConsumer<? super T, X> throwingConsumer) throws X {
        return ifPresent(consumerOf(throwingConsumer));
    }

    /**
     * Throws exception supplied by the exceptionSupplier if the value is present.
     * If the exceptionSupplier or the exception provided by it is null an IllegalArgumentException
     * is thrown. The supplier is assumed to be able to throw an Exception. Returns this.
     */
    /**
     * Throws exception supplied by the exceptionSupplier if the value is present.
     *
     * @param exceptionSupplier The supplier to use to get the exception to be thrown. Ff null an
     * {@link IllegalArgumentException} is thrown.
     * @return Returns this.
     * @throws X This method throws exceptions:<br />
     * 1. if exceptionSupplier is null -> {@link IllegalArgumentException} <br />
     * 2. if exception supplied by exceptionSupplier is null -> {@link IllegalArgumentException} <br />
     * 3. if value is present -> X extends Exception <br />
     * 4. if during exceptionSupplier.get() an exception is thrown -> X extends Exception<br />
     */
    public <X extends Exception> Opt<T> ifPresentThrow(Supplier<X> exceptionSupplier) throws X {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value != null) {
            throw getExceptionOrElseThrowIAE(exceptionSupplier);
        }
        return this;
    }

    /**
     * Inspects whether the value is present or not.
     *
     * @return Returns true if the value is present.
     */
    public boolean isPresent() {
        return (value != null);
    }

    /**
     * Transforms the value from T to U using the mapper.
     *
     * @param mapper The mapper to use for the transformation of the value. If null an {@link IllegalArgumentException}
     * is thrown.
     * @return Returns the transformed value of type U in a new Opt. If the transform returns a null, an Opt.empty is
     * returned.
     */
    public <U> Opt<U> map(Function<? super T, ? extends U> mapper) {
        Validator.validateNotNull("mapper", mapper);
        if (!isPresent()) {
            return empty();
        } else {
            return Opt.of(mapper.apply(value));
        }
    }

    /**
     * Transforms the value from T to U using the mapper.
     *
     * @param mapper The mapper to use for the transformation of the value. If null an {@link IllegalArgumentException}
     * is thrown.
     * @return Returns the transformed value of type U in a new Opt. If the transform returns a null, an Opt.empty is
     * returned.
     * @throws X The tranforming function is assumed to be able to throw an Exception.
     */
    public <X extends Exception, U> Opt<U> map(ThrowingFunction<? super T, ? extends U, X> mapper) throws X {
        return map(functionOf(mapper));
    }

    /**
     * Performs a conditional mapping of the value.
     *
     * @param matchingClass The class to match against, if matchingClass and the values class are a match
     * the matchingMapper is applied. If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transforming function to apply to the value. If null an
     * {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in an Opt:<br />
     * 1. value is present, transformed value is not a null -> BiOpt(Opt(original), Opt(transformedValue)) <br />
     * 2. value is present, transform returns a null -> BiOpt(Opt(original), Opt(empty))
     */
    public <R, U> BiOpt<T, ?> match(Class<R> matchingClass, Function<? super R, ? extends U> matchingMapper) {
        Validator.validateNotNull("matchingClass", matchingClass);
        Validator.validateNotNull("matchingMapper", matchingMapper);

        if (isPresent() && matchingClass.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            R matchedValue = (R) value;

            U transformedValue = matchingMapper.apply(matchedValue);

            return BiOpt.of(value, transformedValue);
        }
        return BiOpt.of(value, null);
    }

    /**
     * Performs a conditional mapping of the value.
     *
     * @param matchingClass The class to match against, if matchingClass and the values class are a match
     * the matchingMapper is applied. If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transforming function to apply to the value. If null an
     * {@link IllegalArgumentException} is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in an Opt:<br />
     * 1. value is present, transformed value is not a null -> BiOpt(Opt(original), Opt(transformedValue)) <br />
     * 2. value is present, transform returns a null -> BiOpt(Opt(original), Opt(empty))
     * @throws X The matchingMapper is assumed to be able to throw an Exception.
     */
    public <R, U, X extends Exception> BiOpt<T, ?> match(Class<R> matchingClass, ThrowingFunction<? super R, ? extends U, X> matchingMapper) throws X {
        return match(matchingClass, functionOf(matchingMapper));
    }

    /**
     * Performs a conditional consuming of the value.
     *
     * @param matchingClass The class to match against, if matchingClass and the values class are a match
     * the matchingConsumer is applied. If null an {@link IllegalArgumentException} is thrown.
     * @param matchingConsumer The consumer to apply to the value. If null an {@link IllegalArgumentException}
     * is thrown.
     * @return Returns this.
     */
    public <V extends Object> Opt<T> match(V matchingValue, Consumer<V> matchingConsumer) {
        Validator.validateNotNull("matchingValue", matchingValue);
        Validator.validateNotNull("matchingConsumer", matchingConsumer);

        if (isTypeRefMatchWithValue(value, matchingValue)) {

            @SuppressWarnings("unchecked")
            V matchedValue = (V) value;

            matchingConsumer.accept(matchedValue);
        }
        return this;
    }

    /**
     * Performs a conditional T to R transformation using a pattern match.
     * The types of value and typeRef may be different. If they match, the predicate is tested,
     * and finally the matchingMapper is applied if all conditions match.
     *
     * @param typeRef The type reference object to match against the values class. <br />If null an
     * {@link IllegalArgumentException} is thrown.
     * @param predicate The predicate to test, if tests true, matchingMapper is applied to the value.
     * <br />If null an {@link IllegalArgumentException} is thrown.
     * @param matchingMapper The transformation function to apply to. <br />If null an {@link IllegalArgumentException}
     * is thrown.
     * @return Returns a new BiOpt containing the original Opt and the transformed value in a new Opt.
     * The transformedValue may be null -> Opt.empty:
     * <pre>
     * Is value  Does typeRef Does value?  Is matchingMapper?  Returned BiOpt:
     * present?  match?       class match  applied
     * yes       yes          yes          yes                 BiOpt(original, transformedValue**)
     * yes       yes          no           no                  BiOpt(original, empty)
     * yes       no           *            no                  BiOpt(original, empty)
     * no        *            *            no                  BiOpt(original, empty)
     * * == for yes or no.
     * ** == may be null.
     * </pre>
     */
    public <V, R> BiOpt<T, ?> match(V typeRef, Predicate<V> predicate, Function<V, R> matchingMapper) {
        Validator.validateNotNull("typeRef", typeRef);
        Validator.validateNotNull("predicate", predicate);
        Validator.validateNotNull("matchingMapper", matchingMapper);

        @SuppressWarnings("unchecked")
        Opt<R> newRight = this.filter(t -> isTypeRefClassMatchWithValueClass(t, typeRef))
                              .map(t -> (V) t)
                              .filter(predicate)
                              .map(matchingMapper);

        return BiOpt.of(value, newRight.get());
    }

    /**
     * Performs a conditional consuming of the value using a pattern match.
     * If value.getClass() == matchingValue.getClass() and the value is equal to matchingValue the
     * matchingConsumer is applied. Returns this.
     */
    public <V extends Object, X extends Exception> Opt<T> match(V matchingValue, ThrowingConsumer<V, X> matchingConsumer) throws X {
        return match(matchingValue, consumerOf(matchingConsumer));
    }

    /**
     * Performs a conditional transformation using a pattern match.
     * If value.getClass() == typeRef.getClass() and the predicate evaluates to true
     * the matchingMapper is applied to the value. Both the value and the
     * transformedValue are stored in a new BiOpt.of(value, transformedValue). If there is no
     * match or the value is not present a new BiOpt.of(Opt.of(value), Opt.empty()) is returned.
     * If typeRef, predicate or matchingMapper is null an IllegalArgumentException is thrown.
     * The predicate and the matchingMapper are assumed to be able to throw an Exception.
     */
    public <V, R, X extends Exception> BiOpt<T, ?> match(V typeRef, ThrowingPredicate<V, X> predicate, ThrowingFunction<V, R, X> matchingMapper) throws X {
        return match(typeRef, predicateOf(predicate), functionOf(matchingMapper));
    }

    @Override
    public String toString() {
        return ToStringBuilder.of(this)
                              .add(t -> String.valueOf(t.value))
                              .build();
    }

    private <X extends Exception> X getExceptionOrElseThrowIAE(Supplier<X> exceptionSupplier) {
        return Opt.of(exceptionSupplier.get())
                  .getOrElseThrow(() -> new IllegalArgumentException("the value of the exceptionSupplier can not be null."));
    }

    private <V> boolean isTypeRefClassMatchWithValueClass(T value, V typeRef) {
        return value != null &&
               typeRef != null &&
               typeRef.getClass().isAssignableFrom(value.getClass());
    }

    private <V> boolean isTypeRefMatchWithValue(T value, V typeRef) {
        return value != null &&
               typeRef != null &&
               typeRef.getClass().isAssignableFrom(value.getClass()) &&
               typeRef.equals(value);
    }
}
