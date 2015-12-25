package mg.util.functional.function;

import org.junit.Test;

public class ThrowingBiFunctionTest {


    @Test(expected = Exception.class)
    public void test() {

        ThrowingBiFunction<String, String, String, Exception> throwingBiFunction = (t, u) -> {
            throw new Exception("msg");
        };

        throwingBiFunction.apply("", "");
    }

}
