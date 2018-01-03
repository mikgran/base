package mg.util.functional.function;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;

import org.junit.Test;

public class ThrowingFunctionTest {

    @Test(expected = Exception.class)
    public void test() {

        ThrowingFunction<String, String, Exception> throwingFunction = (t) -> {
            throw new Exception("msg");
        };

        throwingFunction.apply("");
    }

    @Test
    public void testAsThrows() {

        Function<Long, String> function1 = l -> l.toString();

        @SuppressWarnings("unused")
        ThrowingFunction<Long, String, ?> function2 = ThrowingFunction.of(function1);

        Function<String, Long> function3 = s -> Long.parseLong(s);

        ThrowingFunction<String, Long, NumberFormatException> function4 = ThrowingFunction.of(function3);

        RuntimeException rte = assertThrows(RuntimeException.class, () -> function4.apply(""));
        assertEquals(NumberFormatException.class, rte.getCause().getClass());
    }

}
