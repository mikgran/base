package mg.util.functional.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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
    public void testFilter() {

        {
            BiOpt<String, ?> biOpt;
            biOpt = BiOpt.of("left", "right")
                         .filterLeft("left"::equals);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals("left", biOpt.left().get());
            assertEquals("left", biOpt.right().get());

            biOpt = biOpt.filterLeft("otherValue"::equals);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals("left", biOpt.left().get());
            assertNull(biOpt.right().get());

            biOpt = BiOpt.of("left", "right")
                         .filterRight("right"::equals);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals("left", biOpt.left().get());
            assertEquals("right", biOpt.right().get());

            biOpt = biOpt.filterRight("otherValue"::equals);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals("left", biOpt.left().get());
            assertNull(biOpt.right().get());
        }
        {
            assertThrows(IllegalArgumentException.class, () -> BiOpt.of("left", "right")
                                                                    .filterLeft(null));

            assertThrows(Exception.class, () -> BiOpt.of("left", "right")
                                                     .filterLeft(s -> {
                                                         throw new Exception();
                                                     }));

            assertThrows(IllegalArgumentException.class, () -> BiOpt.of("left", "right")
                                                                    .filterRight(null));

            assertThrows(Exception.class, () -> BiOpt.of("left", "right")
                                                     .filterRight(s -> {
                                                         throw new Exception();
                                                     }));
            try {

                BiOpt.of("left", (String) null)
                     .filterRight(s -> {
                         if (s.length() == 3) {
                             throw new Exception();
                         }
                         return true;
                     });

            } catch (Exception e) {
                fail("there should be no exception for filterRight with Opt(null)");
            }
        }
        {
            try {

                BiOpt.of((String) null, "right")
                     .filterLeft(s -> {
                         if (s.length() == 1) {
                             throw new Exception();
                         }
                         return true;
                     });

            } catch (Exception e) {

            }
        }
    }

    @Test
    public void testIfEmpty() {
        {
            BiOpt<String, String> biOpt;
            biOpt = BiOpt.of("left", "right")
                         .ifLeftEmpty(() -> "value");

            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals("right", biOpt.right().get());
        }
        {
            BiOpt<String, String> biOpt = BiOpt.of((String) null, "right");

            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals(null, biOpt.left().get());
            assertEquals("right", biOpt.right().get());

            biOpt = biOpt.ifLeftEmpty(() -> "left");

            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals("right", biOpt.right().get());
        }
        {
            BiOpt<String, String> biOpt = BiOpt.of("left", (String) null);

            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals(null, biOpt.right().get());

            biOpt = biOpt.ifRightEmpty(() -> "right");

            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals("right", biOpt.right().get());

        }
        {
            assertThrows(IllegalArgumentException.class, () -> BiOpt.of("left", "right")
                                                                    .ifLeftEmpty(null));

            assertThrows(Exception.class, () -> BiOpt.of(null, "right")
                                                     .ifLeftEmpty(() -> {
                                                         throw new Exception();
                                                     }));

            assertThrows(IllegalArgumentException.class, () -> BiOpt.of("left", "right")
                                                                    .ifRightEmpty(null));

            assertThrows(Exception.class, () -> BiOpt.of("left", null)
                                                     .ifRightEmpty(() -> {
                                                         throw new Exception();
                                                     }));
            try {
                BiOpt.of("left", "right")
                     .ifLeftEmpty(() -> {
                         throw new Exception();
                     });
            } catch (Exception e) {
                fail("no exception expected from ifLeftEmpty");
            }

            try {
                BiOpt.of("left", "right")
                     .ifRightEmpty(() -> {
                         throw new Exception();
                     });
            } catch (Exception e) {
                fail("no exception expected from ifRightEmpty");
            }
        }

    }

    // @Disabled
    @Test
    public void testMatch() {

        {
            BiOpt<String, ?> match = BiOpt.of("left", "right")
                                          .match(Integer.class, i -> String.valueOf(i).length())
                                          .match(String.class, s -> s.length())

            ;

            // System.out.println(match);
            // fail("test for matchLeft, matchRight missing, BiOpt returns fail atm.");
        }
        {
            BiOpt<String, ?> biOpt = BiOpt.of("left", "right")
                                          .matchPattern("", s -> s.length() == 3, s -> s + "Postfix");
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals("right", biOpt.right().get());
        }
        {
            BiOpt<String, ?> biOpt = BiOpt.of("left", "right")
                                          .matchPattern("", "left"::equals, s -> s + "Postfix")
                                          .matchPattern(0, i -> i == 3, i -> i + 1);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals("leftPostfix", biOpt.right().get());
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
