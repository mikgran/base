package mg.util.functional.consumer;

import java.util.function.Consumer;

// boldly borrowing someone's solution right here.
/**
 * A consumer which overrides the Exceptionless Consumer.accept(T t) 
 * and delegates it to acceptThrows(T t) that throws an Exception.
 * Access the cause via exception.getCause() to get the proper
 * stackTrace instead of the functional framework trace. Since the 
 * exception that is being thrown is a RuntimeException it may be
 * necessary to convert the exception back to checked exception.
<code>
    <pre>
Usage:
    List&ltString&gt list = Arrays.asList("A", "B", "C");
    ThrowingConsumer&ltString&gt throwingConsumer = a -> {
        // replace this with exception generating functionality
        throw new Exception("msg"); 
    };
    list.forEach(throwingConsumer);
   
Or:
    list.forEach((ThrowingConsumer<String>) a -> {
        // replace this with exception generating functionality
        throw new Exception("msg");
    });
    </pre>
</code>
 * @param <T> Type T to accept(T t) by super class Consumer<T>
 */
@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {

    @Override
    default void accept(T t) {

        try {

            acceptThrows(t);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T t) throws Exception;
}
