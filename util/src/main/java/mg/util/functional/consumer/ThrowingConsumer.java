package mg.util.functional.consumer;

import java.util.function.Consumer;

// boldly borrowing someone's solution right here.
/**
 * A consumer which overrides the Exceptionless Consumer.accept(T t)
 * and delegates it to acceptThrows(T t) that throws a RuntimeException.
 * Access the cause via the exception.getCause() to get the proper
 * stackTrace instead of the functional framework trace.
 *
<code>
    <pre>
Usage:
    try {
        List&ltString&gt list = Arrays.asList("A", "B", "C");
        ThrowingConsumer&ltString, Exception&gt throwingConsumer = a -> {
            // replace this with exception generating functionality
            throw new Exception("msg");
        };
        list.forEach(throwingConsumer);
    } catch (RuntimeException e) {
        // unwrapAndRethrow(e);
    }
Or:
    try {
        list.forEach((ThrowingConsumer&ltString, Exception&gt) a -> {
            // replace this with exception generating functionality
            throw new Exception("msg");
        });
    } catch (RuntimeException e) {
        // unwrapAndRethrow(e);
    }
    </pre>
</code>
 * @param <T> Type T to accept(T t) by super class Consumer<T>
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> extends Consumer<T> {

    @Override
    default void accept(T t) {

        try {

            acceptThrows(t);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T t) throws E;
}
