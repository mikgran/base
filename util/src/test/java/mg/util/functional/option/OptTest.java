package mg.util.functional.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class OptTest {

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

        // 7. getOrElse getOrElseGet
        Opt<String> optGetOrElse = Opt.of("getOrElse");
        assertEquals("getOrElse", optGetOrElse.getOrElse("other"));
        optGetOrElse = Opt.of(null);
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

        // 8. ifPresent
        // - consume without exception
        // - consume throwing exception
        Opt<String> optIfPresent = Opt.of("optIfPresent");
        StringBuilder sb = new StringBuilder();
        assertEquals("", sb.toString());
        optIfPresent.ifPresent(sb::append);
        assertEquals("optIfPresent", sb.toString());

        assertThrows(Exception.class, () -> optIfPresent.ifPresent(s -> {
            throw new Exception();
        }));

        // 9. isPresent
        assertTrue(Opt.of("isPresent").isPresent());
        assertFalse(Opt.of(null).isPresent());
        assertFalse(Opt.empty().isPresent());

        // 10. orElseThrow
        final Opt<String> optOrElseThrow = Opt.empty();
        assertThrows(TestException.class, () -> optOrElseThrow.orElseThrow(() -> new TestException()));
        Opt<String> optOrElseThrow2 = Opt.of("optOrElseThrow");

        try {
            assertEquals("optOrElseThrow", optOrElseThrow2.orElseThrow(() -> new TestException()));
        } catch (Throwable e) {
            fail("no exception should be thrown for Opt(\"value\")" + e.getMessage());
        }

        Opt<Object> optEmpty2 = Opt.empty();
        IllegalArgumentException illegalArgumentException2 = assertThrows(IllegalArgumentException.class, () -> optEmpty2.orElseThrow(null));
        assertEquals("exceptionSupplier can not be null.", illegalArgumentException2.getMessage());
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
