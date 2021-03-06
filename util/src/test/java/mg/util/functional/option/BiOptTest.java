package mg.util.functional.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// TOIMPROVE: add more RTE -> RTE.getCause() == targetException tests.
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
            Opt<String> left = biOpt.map(a -> a + "1")
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
        {
            Opt<String> left = biOpt.left();
            Opt<String> right = biOpt.right();

            Assertions.assertThrows(IllegalArgumentException.class, () -> left.map(null));
            Assertions.assertThrows(IllegalArgumentException.class, () -> right.map(null));
        }
    }

    @Test
    public void testFilter() {

        {
            BiOpt<String, ?> biOpt;
            biOpt = BiOpt.of("left", "right")
                         .filter("left"::equals);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals("left", biOpt.left().get());
            assertEquals("left", biOpt.right().get());

            biOpt = biOpt.filter("otherValue"::equals);
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
                                                                    .filter(null));

            assertThrows(Exception.class, () -> BiOpt.of("left", "right")
                                                     .filter(s -> {
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
                     .filter(s -> {
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

    @Disabled
    @Test
    public void testMatch() {
        {
            fail("test the BiOpt.left/right assignments");
        }
    }

    @Test
    public void testMatchPatternConsumer() {
        {
            StringBuilder sb = new StringBuilder();
            BiOpt<String, ?> biOpt = BiOpt.of("left", "right")
                                          .match("", s -> s.length() == 4, s -> {
                                              sb.append(s);
                                          });
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals("right", biOpt.right().get());
            assertEquals("left", sb.toString());
        }
        {
            RuntimeException rte = Assertions.assertThrows(RuntimeException.class, () -> {
                BiOpt.of("left", "right")
                     .match("", (String s) -> s.length() == 4, (String s1) -> {
                         throw new TestException();
                     });
            });
            assertEquals(TestException.class, rte.getCause().getClass());
        }
    }

    @Test
    public void testMatchPatternMapper() {
        {
            BiOpt<String, ?> biOpt = BiOpt.of("left", "right")
                                          .match("", s -> s.length() == 3, s -> s + "Postfix");
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
                                          .match("", "left"::equals, s -> s + "Postfix")
                                          .match(0, i -> i == 3, i -> i + 1);
            assertMatchLeftPostFix(biOpt);
        }
        {
            BiOpt<String, ?> biOpt = BiOpt.of("left", "right")
                                          .matchRight("", "left"::equals, s -> s + "Postfix")
                                          .matchRight(0, i -> i == 3, i -> i + 1);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals("right", biOpt.right().get());
        }
        {
            BiOpt<String, ?> biOpt = BiOpt.of("left", 3)
                                          .matchRight("", "left"::equals, s -> s + "Postfix")
                                          .matchRight(0, i -> i == 3, i -> i + 1);
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertNotNull(biOpt.left().get());
            assertNotNull(biOpt.right().get());
            assertEquals("left", biOpt.left().get());
            assertEquals(4, biOpt.right().get());
        }
        // case String, Integer: flows naturally, matches, does not match.
        // matchPatternLeft -> matchPatternRight ->
        // left             -> leftPostfix       -> leftPostFix
        {
            BiOpt<String, ?> biOpt = BiOpt.of("left", 3)
                                          .match("", "left"::equals, s -> s + "Postfix")
                                          .matchRight(0, i -> i == 3, i -> i + 1);
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

            biOpt.match(1, noOpConsumer());
            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals(Integer.valueOf(201), biOpt.left().get());
            assertEquals(Integer.valueOf(1), biOpt.right().get());
            assertEquals("", sb.toString());

            biOpt.match(201, sb::append);
            assertEquals("201", sb.toString());

            StringBuilder sb2 = new StringBuilder();
            biOpt.matchRight(1, v -> sb2.append(v + 1));
            assertEquals("2", sb2.toString());
        }
    }

    private void assertMatchLeftPostFix(BiOpt<String, ?> biOpt) {
        assertNotNull(biOpt);
        assertNotNull(biOpt.left());
        assertNotNull(biOpt.right());
        assertNotNull(biOpt.left().get());
        assertNotNull(biOpt.right().get());
        assertEquals("left", biOpt.left().get());
        assertEquals("leftPostfix", biOpt.right().get());
    }

    private Consumer<Integer> noOpConsumer() {
        return v -> {
        };
    }

    public class TestException extends Exception {

        private static final long serialVersionUID = -1L;
    }
}
