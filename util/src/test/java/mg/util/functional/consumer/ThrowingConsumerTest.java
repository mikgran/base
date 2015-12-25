package mg.util.functional.consumer;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ThrowingConsumerTest {

    @Test(expected = Exception.class)
    public void test() throws Exception {

        List<String> list = Arrays.asList("A", "B", "C");

        ThrowingConsumer<String, Exception> throwingConsumer = a -> {
            throw new Exception("msg");
        };

        list.forEach(throwingConsumer);
    }
}
