package mg.util.functional.option;

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

    public <X extends Exception> Opt<T> filter(ThrowingPredicate<? super T, X> predicate) throws X {
        Validator.validateNotNull("predicate", predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
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

    public T get() {
        return value;
    }

    public <U> U getAndMap(Function<T, ? extends U> mapper) {
        Validator.validateNotNull("mapper", mapper);
        if (isPresent()) {
            return mapper.apply(value);
        } else {
            return null;
        }
    }

    public T getOrElse(T other) {
        return value != null ? value : other;
    }

    public T getOrElseGet(Supplier<? extends T> supplier) {
        Validator.validateNotNull("supplier", supplier);
        return value != null ? value : supplier.get();
    }

    public <X extends Exception> T getOrElseGet(ThrowingSupplier<? extends T, X> supplier) throws X {
        Validator.validateNotNull("supplier", supplier);
        return value != null ? value : supplier.get();
    }

    public T getOrElseThrow(Supplier<? extends Throwable> exceptionSupplier) throws Throwable {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public <E extends Exception> T getOrElseThrow(ThrowingSupplier<? extends Throwable, E> exceptionSupplier) throws Throwable {
        Validator.validateNotNull("exceptionSupplier", exceptionSupplier);
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
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

    public <X extends Exception> Opt<T> ifEmpty(ThrowingSupplier<T, X> supplier) throws X {
        Validator.validateNotNull("supplier", supplier);
        if (value == null) {
            return Opt.of(supplier.get());
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

    public <X extends Exception> Opt<T> ifPresent(ThrowingConsumer<? super T, X> consumer) throws X {
        Validator.validateNotNull("consumer", consumer);
        if (value != null) {
            consumer.accept(value);
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
        Validator.validateNotNull("mapper", mapper);
        if (!isPresent()) {
            return empty();
        } else {
            return Opt.of(mapper.apply(value));
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.of(this)
                              .add(t -> (t == null) ? "" : t.toString())
                              .build();
    }
}
