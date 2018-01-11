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

public class Opt<T> {

    private static final Opt<?> EMPTY = new Opt<>();
    private final T value;

    public static <T> Opt<T> empty() {
        @SuppressWarnings("unchecked")
        Opt<T> t = (Opt<T>) EMPTY;
        return t;
    }

    public static <T> Opt<T> of(Opt<T> opt) {
        return opt == null || !opt.isPresent() ? empty() : new Opt<>(opt.get());
    }

    public static <T> Opt<T> of(Optional<T> optional) {
        return optional == null || !optional.isPresent() ? empty() : new Opt<>(optional.get());
    }

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

    public Opt<T> filter(Predicate<? super T> predicate) {
        Validator.validateNotNull("predicate", predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    public <X extends Exception> Opt<T> filter(ThrowingPredicate<? super T, X> throwingPredicate) throws X {
        return filter(predicateOf(throwingPredicate));
    }

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

    public <X extends Exception, U> Opt<U> flatMap(ThrowingFunction<? super T, Opt<U>, X> mapper) throws X {
        return flatMap(functionOf(mapper));
    }

    public T get() {
        return value;
    }

    public <U> U getAndMap(Function<? super T, ? extends U> mapper) {
        Validator.validateNotNull("mapper", mapper);
        if (isPresent()) {
            return mapper.apply(value);
        } else {
            return null;
        }
    }

    public <U, X extends Exception> U getAndMap(ThrowingFunction<? super T, ? extends U, X> mapper) throws X {
        return getAndMap(functionOf(mapper));
    }

    public T getOrElse(T other) {
        return value != null ? value : other;
    }

    public T getOrElseGet(Supplier<? extends T> supplier) {
        Validator.validateNotNull("supplier", supplier);
        return value != null ? value : supplier.get();
    }

    public <X extends Exception> T getOrElseGet(ThrowingSupplier<? extends T, X> supplier) throws X {
        return getOrElseGet(supplierOf(supplier));
    }

    public <X extends Exception> T getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value != null) {
            return value;
        } else {
            throw getExceptionOrElseThrowIAE(exceptionSupplier);
        }
    }

    public <X extends Exception> T getOrElseThrow(ThrowingSupplier<X, X> exceptionSupplier) throws X {
        return getOrElseThrow(supplierOf(exceptionSupplier));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public Opt<T> ifEmpty(Supplier<T> supplier) {
        Validator.validateNotNull("supplier", supplier);
        if (value == null) {
            return Opt.of(supplier.get());
        }
        return this;
    }

    public <X extends Exception> Opt<T> ifEmpty(ThrowingSupplier<T, X> throwingSupplier) throws X {
        return ifEmpty(supplierOf(throwingSupplier));
    }

    public <X extends Exception> Opt<T> ifEmptyThrow(Supplier<X> exceptionSupplier) throws X {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value == null) {
            throw getExceptionOrElseThrowIAE(exceptionSupplier);
        }
        return this;
    }

    public Opt<T> ifPresent(Consumer<? super T> consumer) {
        Validator.validateNotNull("consumer", consumer);
        if (value != null) {
            consumer.accept(value);
        }
        return this;
    }

    public <X extends Exception> Opt<T> ifPresent(ThrowingConsumer<? super T, X> throwingConsumer) throws X {
        return ifPresent(consumerOf(throwingConsumer));
    }

    public <X extends Exception> Opt<T> ifPresentThrow(Supplier<X> exceptionSupplier) throws X {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value != null) {
            throw getExceptionOrElseThrowIAE(exceptionSupplier);
        }
        return this;
    }

    public boolean isPresent() {
        return (value != null);
    }

    public <U> Opt<U> map(Function<? super T, ? extends U> mapper) {
        Validator.validateNotNull("mapper", mapper);
        if (!isPresent()) {
            return empty();
        } else {
            return Opt.of(mapper.apply(value));
        }
    }

    public <X extends Exception, U> Opt<U> map(ThrowingFunction<? super T, ? extends U, X> mapper) throws X {
        return map(functionOf(mapper));
    }

    /**
     * Performs conditional mapping of the value of the left. If the left.getClass == matchingClass the matchingMapper
     * is applied to the left and the result is stored in a new BiOpt.right, the left is stored in BiOpt.left.
     * If there is no match, a null value is stored in the biOpt.right.
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

    public <R, U, X extends Exception> BiOpt<T, ?> match(Class<R> matchingClass, ThrowingFunction<? super R, ? extends U, X> matchingMapper) throws X {
        return match(matchingClass, functionOf(matchingMapper));
    }

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

    public <V extends Object, X extends Exception> Opt<T> match(V matchingValue, ThrowingConsumer<V, X> matchingConsumer) throws X {
        return match(matchingValue, consumerOf(matchingConsumer));
    }

    /**
     * Performs a pattern match. If value.getClass() == typeRef and the predicate returns true
     * the matchingMapper is applied and the result is stored in a new BiOpt.right.
     * If no match is found or the predicate returns false the right is set as empty.
     * The value is stored in the left.
     */
    public <V, R> BiOpt<T, ?> matchPattern(V typeRef, Predicate<V> predicate, Function<V, R> matchingMapper) {
        Validator.validateNotNull("typeRef", typeRef);
        Validator.validateNotNull("matchingMapper", matchingMapper);

        @SuppressWarnings("unchecked")
        Opt<R> newRight = this.filter(t -> isTypeRefClassMatchWithValueClass(t, typeRef))
                              .map(t -> (V) t)
                              .filter(predicate)
                              .map(matchingMapper);

        return BiOpt.of(value, newRight.get());
    }

    /**
     * Performs a pattern match. If value.getClass() == typeRef and the predicate returns true
     * the matchingMapper is applied and the result is stored in a new BiOpt.right.
     * If no match is found or the predicate returns false the right is set as empty.
     * The value is stored in the left.
     */
    public <V, R, X extends Exception> BiOpt<T, ?> matchPattern(V typeRef, ThrowingPredicate<V, X> predicate, ThrowingFunction<V, R, X> matchingMapper) throws X {
        return matchPattern(typeRef, predicateOf(predicate), functionOf(matchingMapper));
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
