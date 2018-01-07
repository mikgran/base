package mg.util.functional.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

public class BiOptTest {

    @Test
    public void testBiOpt() {

        BiOpt<String, String> biOpt = BiOpt.of("1", "2")
                                           .map(a -> a + "1", b -> b + "2");
        {
            Opt<String> left = biOpt.left();
            Opt<String> right = biOpt.right();

            assertNotNull(left);
            assertNotNull(right);
            assertNotNull(left.get());
            assertNotNull(right.get());
            assertEquals("11", left.get());
            assertEquals("22", right.get());
        }
        {
            Opt<String> left = biOpt.mapLeft(a -> a + "1")
                                    .left();
            Opt<String> right = biOpt.right();

            assertNotNull(left);
            assertNotNull(right);
            assertNotNull(left.get());
            assertNotNull(right.get());
            assertEquals("111", left.get());
            assertEquals("22", right.get());
        }

        {
            Opt<String> left = biOpt.left();
            Opt<String> right = biOpt.mapRight(b -> b + "2")
                                     .right();

            assertNotNull(left);
            assertNotNull(right);
            assertNotNull(left.get());
            assertNotNull(right.get());
            assertEquals("11", left.get());
            assertEquals("222", right.get());
        }

    }

    @Test
    public void testMatchValue() {
        {
            StringBuilder sb = new StringBuilder();
            BiOpt<Integer, Integer> biOpt = BiOpt.of(201, 1);

            biOpt.matchLeftValue(1, noOpConsumer());
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals(Integer.valueOf(201), biOpt.left().get());
            assertEquals(Integer.valueOf(1), biOpt.right().get());
            assertEquals("", sb.toString());

            biOpt.matchLeftValue(201, sb::append);
            assertEquals("201", sb.toString());

            StringBuilder sb2 = new StringBuilder();
            biOpt.matchRightValue(1, v -> sb2.append(v + 1));
            assertEquals("2", sb2.toString());
        }
    }

    private Consumer<Integer> noOpConsumer() {
        return v -> {
        };
    }
}
