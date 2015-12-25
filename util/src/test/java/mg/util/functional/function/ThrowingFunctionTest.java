package mg.util.functional.function;

import org.junit.Test;

public class ThrowingFunctionTest {

    @Test(expected = Exception.class)
    public void test() {

        ThrowingFunction<String, String, Exception> throwingFunction = (t) -> {
            throw new Exception("msg");
        };

        throwingFunction.apply("");
    }

}
