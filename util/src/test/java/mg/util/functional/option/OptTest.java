package mg.util.functional.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import mg.util.functional.supplier.ThrowingSupplier;

public class OptTest {

    @Test
    public void testCaseOf() {

        BiOpt<String, Integer> biOpt = Opt.of("a")
                                      .caseOf(s -> "a".equals(s), s -> 1);

        assertNotNull(biOpt);
        assertEquals(BiOpt.of("a", 1), biOpt);

        BiOpt<String, Integer> biOpt2 = biOpt.caseOf(s -> Integer.valueOf(2).equals(2), s -> 2);

        assertNotNull(biOpt2);
        assertEquals(BiOpt.of("a", 1), biOpt2);
    }

    @Test
    public void testMatchValues() {

        List<Object> asList = Arrays.asList("value1", Integer.valueOf(1), Long.valueOf(2));
        {
            StringBuilder builder = new StringBuilder();
            Opt<Object> optList1 = Opt.of(asList.get(0))
                                      .match("str", noOpConsumer())
                                      .match("value1", builder::append);
            assertNotNull(optList1);
            assertEquals("value1", builder.toString());
        }
        {
            StringBuilder builder = new StringBuilder();
            Opt<Object> optList1 = Opt.of(asList.get(1))
                                      .match("str", noOpConsumer())
                                      .match(Integer.valueOf(1), builder::append);
            assertNotNull(optList1);
            assertEquals("1", builder.toString());
        }
        {
            IllegalArgumentException iae;
            iae = assertThrows(IllegalArgumentException.class, () -> Opt.of(asList.get(2))
                                                                        .match("str", noOpConsumer())
                                                                        .match("value", null));
            assertEquals("matchingConsumer can not be null.", iae.getMessage());
        }
        {
            IllegalArgumentException iae;
            iae = assertThrows(IllegalArgumentException.class, () -> Opt.of(asList.get(0))
                                                                        .match(null, noOpConsumer()));
            assertEquals("matchingValue can not be null.", iae.getMessage());
        }

        // case: caseOf a replacement for the clunky looking switch-case-default syntax
        // design notes: not a full pattern match, just mapper (and consumer?)
        // inspects the BiOpt.right().isEmpty() to execute the caseOf
        // BiOpt.caseOf() should be executed only if there is no right present.
        // Opt.caseOf(predicate, mapper) always executes and produces BiOpt(value, transformed value)
        // Opt.caseOf(predicate, consumer) executes always and produces BiOpt(value, new Object())
        // to make sure only one caseOf is called. Storing null to the BiOpt.right causes subsequent calls
        // of caseOf(predicate, *) to be called again.
        // caseOf is type bound to T, there is no type T and type V isAssignableFrom calls.
        {
            BiOpt<String, ?> biOpt = Opt.of("value")
                                        .caseOf("value"::equals, s -> "" + s.length())
                                        .caseOf("value"::equals, s -> "length: " + s.length());

            assertNotNull(biOpt);
            assertNotNull(biOpt.left());
            assertNotNull(biOpt.right());
            assertEquals("value", biOpt.left().get());
            assertEquals("5", biOpt.right().get());
        }
        {
            assertThrows(Exception.class, () -> Opt.of("value")
                                                   .caseOf("value"::equals, s -> {
                                                       throw new Exception();
                                                   }));

            IllegalArgumentException iae;
            iae = assertThrows(IllegalArgumentException.class, () -> Opt.of("value")
                                                                        .caseOf(null, s -> s));

            assertEquals("predicate can not be null.", iae.getMessage());

            iae = assertThrows(IllegalArgumentException.class, () -> Opt.of("value")
                                                                        .caseOf("value"::equals, null));
            assertEquals("matchingMapper can not be null.", iae.getMessage());
        }
    }

    // TOCONSIDER / TOIMPROVE: splice this into multiple test methods?
    // TOIMPROVE: test coverage, plus all the exception message cases
    @Test
    public void testOpt() {

        // 1. null value

        String nullStr = null;
        Opt<String> optNull = Opt.of(nullStr);

        assertNotNull(optNull);
        assertNull(optNull.get());

        // 2. non null value
        String nonNullStr = "abc";
        Opt<String> optNonNull = Opt.of(nonNullStr);

        assertNotNull(optNonNull);
        assertEquals("abc", optNonNull.get());

        // 3. empty()
        Opt<String> optEmptyStr = Opt.empty();
        assertNotNull(optEmptyStr);
        assertNull(optEmptyStr.get());

        // 4. equals() by value
        Opt<String> optAbc = Opt.of("abc");
        Opt<String> optBcd = Opt.of("bcd");
        Opt<String> optAbc2 = Opt.of("abc");
        Opt<String> optEmptyStrForEquals = Opt.empty();
        assertEquals(optAbc, optAbc2);
        assertNotEquals(optAbc, optBcd);
        assertNotEquals(optAbc, optEmptyStrForEquals);
        assertNotEquals(optAbc, null);

        // 5. filter()
        // - null predicate throwing IllegalArgumentException
        final Opt<String> optFilter = Opt.of("filter");
        assertThrows(IllegalArgumentException.class, () -> optFilter.filter(null));

        // - Opt.empty filter
        Opt<String> optEmpty1 = Opt.empty();
        optEmpty1 = optEmpty1.filter(s -> s.length() == 0);
        assertEquals(null, optEmpty1.get());

        // - Opt.nonEmpty filter true
        Opt<String> optFff = Opt.of("fff");
        optFff = optFff.filter(s -> "fff".equals(s));
        assertEquals("fff", optFff.get());

        // - Opt.nonEmpty filter false
        optFff = optFff.filter(s -> "another".equals(s));
        assertFalse(optFff.isPresent());

        // - predicate throwing Exception
        Opt<String> optSss = Opt.of("sss");
        assertThrows(Exception.class, () -> optSss.filter(s -> {
            throw new Exception();
        }));

        // 6. flatMap
        // - missing mapper throwing an IllegalArgumentException
        // - Opt.empty flatMap
        // - Opt.nonEmpty flatMap
        // - mapper throwing an Exception
        TestClass1 tc1 = new TestClass1();
        Opt<TestClass1> optTc1 = Opt.of(tc1);
        {
            String str1 = optTc1.flatMap(t -> t.tc2)
                                .flatMap(t2 -> t2.str1)
                                .get();
            assertEquals("str1", str1);

            String str2 = optTc1.flatMap(t -> t.tc2)
                                .map(t2 -> t2.str2)
                                .get();
            assertEquals("str2", str2);

            String str3 = optTc1.flatMap(t -> t.tc2)
                                .flatMap(t2 -> t2.str3)
                                .get();
            assertNull(str3);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> optTc1.flatMap(null));
            assertEquals("mapper can not be null.", exception.getMessage());

            assertThrows(Exception.class, () -> optTc1.flatMap(t -> {
                throw new Exception();
            }));

            assertThrows(Exception.class, () -> optTc1.map(t -> {
                throw new Exception();
            }));
        }

        // 7. getOrElse getOrElseGet
        {
            Opt<String> optGetOrElse = Opt.of("getOrElse");
            assertEquals("getOrElse", optGetOrElse.getOrElse("other"));
            optGetOrElse = Opt.of((String) null);
            assertEquals("other", optGetOrElse.getOrElse("other"));
            optGetOrElse = Opt.empty();
            assertEquals("other", optGetOrElse.getOrElse("other"));

            Opt<String> optGetOrElseGet = Opt.of("getOrElseGet");
            assertEquals("getOrElseGet", optGetOrElseGet.getOrElseGet(() -> "other"));
            optGetOrElseGet = Opt.empty();
            assertEquals("other", optGetOrElseGet.getOrElseGet(() -> "other"));
            final Opt<String> optGetOrElseGet2 = Opt.of("getOrElseGet");
            IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> optGetOrElseGet2.getOrElseGet(null));
            assertEquals("supplier can not be null.", exception2.getMessage());

            final Opt<String> optGetOrElseGet3 = Opt.empty();
            assertThrows(Exception.class, () -> optGetOrElseGet3.getOrElseGet(() -> {
                throw new Exception();
            }));

            IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> optTc1.map(null));
            assertEquals("mapper can not be null.", exception3.getMessage());
        }

        // 8. ifPresent, IfEmpty
        // - consume without exception
        // - consume while throwing exception
        {
            Opt<String> optIfPresent = Opt.of("optIfPresent");
            StringBuilder sb = new StringBuilder();
            assertEquals("", sb.toString());
            optIfPresent.ifPresent(sb::append);
            assertEquals("optIfPresent", sb.toString());

            assertThrows(Exception.class, () -> optIfPresent.ifPresent(s -> {
                throw new Exception();
            }));
        }
        {
            Opt<String> opt = Opt.of("optIfEmpty");
            StringBuilder sb = new StringBuilder();
            assertEquals("", sb.toString());
            opt.ifEmpty(o -> {
                sb.append("a");
            });
            assertEquals("a", sb.toString());

            assertThrows(Exception.class, () -> opt.ifEmpty(o -> {
                throw new Exception();
            }));

            IllegalArgumentException iae;
            iae = assertThrows(IllegalArgumentException.class, () -> opt.ifEmpty((Consumer<String>) null));
            assertEquals("consumer can not be null.", iae.getMessage());
        }
        {
            String nullStr1 = null;
            String str4 = Opt.of(nullStr1)
                             .ifEmpty(() -> "value")
                             .get();
            assertEquals("value", str4);

            assertThrows(Exception.class, () -> Opt.empty()
                                                   .ifEmpty(() -> {
                                                       throw new Exception();
                                                   }));

            String ifEmpty2 = Opt.of("value1")
                                 .ifEmpty(() -> "value2")
                                 .get();
            assertNotNull(ifEmpty2);
            assertEquals("value1", ifEmpty2);

            IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> Opt.empty()
                                                                                                 .ifEmpty(getNullSupplier()));
            assertEquals("supplier can not be null.", iae.getMessage());
            // and the value of the supplier is allowed to be null.
        }

        // 9. isPresent
        {
            assertTrue(Opt.of("isPresent").isPresent());
            assertFalse(Opt.of((String) null).isPresent());
            assertFalse(Opt.empty().isPresent());
        }

        // 10. getOrElseThrow
        {
            final Opt<String> optOrElseThrow = Opt.empty();
            assertThrows(TestException.class, () -> optOrElseThrow.getOrElseThrow(() -> new TestException()));
            Opt<String> optOrElseThrow2 = Opt.of("optOrElseThrow");

            try {
                assertEquals("optOrElseThrow", optOrElseThrow2.getOrElseThrow(() -> new TestException()));
            } catch (Throwable e) {
                fail("no exception should be thrown for Opt(\"value\")" + e.getMessage());
            }

            Opt<Object> optEmpty2 = Opt.empty();
            IllegalArgumentException illegalArgumentException2 = assertThrows(IllegalArgumentException.class, () -> optEmpty2.getOrElseThrow(null));
            assertEquals("exceptionSupplier can not be null.", illegalArgumentException2.getMessage());

            IllegalArgumentException illegalArgumentException3 = assertThrows(IllegalArgumentException.class, () -> optEmpty2.getOrElseThrow(() -> null));
            assertEquals("the value of the exceptionSupplier can not be null.", illegalArgumentException3.getMessage());
        }

        // 10. Opt.of(Optional.ofNullable("value"))
        {
            Optional<String> optionalNull = Optional.ofNullable(null);
            Opt<String> optOptionalNull = Opt.of(optionalNull);
            assertNotNull(optOptionalNull);
            assertEquals(null, optOptionalNull.get());

            Optional<String> optionalNotNull = Optional.ofNullable("value");
            Opt<String> optOptionalNotNull = Opt.of(optionalNotNull);
            assertNotNull(optOptionalNotNull);
            assertEquals("value", optOptionalNotNull.get());
        }

        // 11. Opt -> Optional
        {
            Optional<String> optionalGetAndMap = Opt.of("value")
                                                    .getAndMap(Optional::ofNullable);
            assertNotNull(optionalGetAndMap);
            assertEquals("value", optionalGetAndMap.get());
        }

        // 12. ifEmptyThrow ifPresentThrow
        {
            Opt<Object> optIfEmptyThrow = Opt.empty();

            Exception e1 = assertThrows(Exception.class,
                                        () -> optIfEmptyThrow.ifEmptyThrow(() -> new Exception("msg")));
            assertEquals("msg", e1.getMessage());
            IllegalArgumentException iae1 = assertThrows(IllegalArgumentException.class,
                                                         () -> optIfEmptyThrow.ifEmptyThrow(() -> null));
            assertEquals("the value of the exceptionSupplier can not be null.", iae1.getMessage());
            IllegalArgumentException iae2 = assertThrows(IllegalArgumentException.class,
                                                         () -> optIfEmptyThrow.ifEmptyThrow(null));
            assertEquals("exceptionSupplier can not be null.", iae2.getMessage());

            Opt<String> optIfEmptyThrow2 = Opt.of("ifEmptyThrow");

            try {
                optIfEmptyThrow2.ifEmptyThrow(() -> new Exception());
            } catch (Throwable e) {
                fail("no exception should be thrown if there is value present.");
            }

            Opt<Object> optIfPresent2 = Opt.of("value");
            Exception e2 = assertThrows(Exception.class, () -> optIfPresent2.ifPresentThrow(() -> new Exception("msg2")));

            assertEquals("msg2", e2.getMessage());
            IllegalArgumentException iaep1 = assertThrows(IllegalArgumentException.class,
                                                          () -> optIfPresent2.ifPresentThrow(() -> null));
            assertEquals("the value of the exceptionSupplier can not be null.", iaep1.getMessage());
            IllegalArgumentException iaep2 = assertThrows(IllegalArgumentException.class,
                                                          () -> optIfPresent2.ifPresentThrow(null));
            assertEquals("exceptionSupplier can not be null.", iaep2.getMessage());

            Opt<String> optIfPresent3 = Opt.empty();

            try {
                optIfPresent3.ifPresentThrow(() -> new Exception());
            } catch (Throwable e) {
                fail("no exception should be thrown if there is value present.");
            }
        }
    }

    @Test
    public void testOptMatch() {
        // matching interop with BiOpt
        {
            BiOpt<Object, ?> biOpt = Opt.of("value")
                                        .map(s -> (Object) s)
                                        .match(String.class, (String s) -> s); // Opt<T>.match(class, mapper)
            BiOpt<Object, ?> biOpt2 = biOpt.match(Long.class, (Long l) -> l); // BiOpt<T, U>.matchLeft(class, leftMapper)

            assertNotNull(biOpt);
            assertNotNull(biOpt2);
            assertNotNull(biOpt.right());
            assertNotNull(biOpt2.right());
            assertEquals("value", biOpt.right().get());

        }
        {
            BiOpt<String, ?> biOpt = Opt.of("1")
                                        .match(String.class, (String s) -> Integer.valueOf(s))
                                        .matchRight(Integer.class, i -> i + 1);

            assertNotNull(biOpt);

            Opt<String> left = biOpt.left();
            Opt<?> right = biOpt.right();

            assertNotNull(left);
            assertNotNull(right);
            assertEquals("1", left.get());
            assertEquals(2, right.get());
        }
    }

    private ThrowingSupplier<Object, Exception> getNullSupplier() {
        return null;
    }

    private Consumer<String> noOpConsumer() {
        return s -> {
        };
    }

    private class TestClass1 {
        public Opt<TestClass2> tc2 = Opt.of(new TestClass2());
    }

    private class TestClass2 {
        public Opt<String> str1 = Opt.of("str1");
        public String str2 = "str2";
        public Opt<String> str3 = Opt.empty();
    }

    private class TestException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
