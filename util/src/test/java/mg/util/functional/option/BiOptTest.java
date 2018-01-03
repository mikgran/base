package mg.util.functional.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class BiOptTest {

    @Test
    public void testBiOpt() {

        BiOpt<String, String> biOpt = BiOpt.of("1", "2")
                                           .map(a -> a + "1", b -> b + "2");
        {
            Opt<String> left = biOpt.getLeft();
            Opt<String> right = biOpt.getRight();

            assertNotNull(left);
            assertNotNull(right);
            assertNotNull(left.get());
            assertNotNull(right.get());
            assertEquals("11", left.get());
            assertEquals("22", right.get());
        }
        {
            Opt<String> left = biOpt.mapLeft(a -> a + "1")
                                    .getLeft();
            Opt<String> right = biOpt.getRight();

            assertNotNull(left);
            assertNotNull(right);
            assertNotNull(left.get());
            assertNotNull(right.get());
            assertEquals("111", left.get());
            assertEquals("22", right.get());
        }

        {
            Opt<String> left = biOpt.getLeft();
            Opt<String> right = biOpt.mapRight(b -> b + "2")
                                    .getRight();

            assertNotNull(left);
            assertNotNull(right);
            assertNotNull(left.get());
            assertNotNull(right.get());
            assertEquals("11", left.get());
            assertEquals("222", right.get());
        }

    }
}
