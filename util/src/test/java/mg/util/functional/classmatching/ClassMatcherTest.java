package mg.util.functional.classmatching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import mg.util.functional.consumer.ThrowingConsumer;

public class ClassMatcherTest {

    private static final String FALLTHROUGH_MESSAGE = "fallthrough message";
    private static final String EXPECTED_FULL_FALLTHROUGH_MESSAGE = "java.lang.Exception: " + FALLTHROUGH_MESSAGE;

    @Test
    public void test() {

        ClassMatcher testKeyMatcher;

        testKeyMatcher = ClassMatcher.matcher()
                                     .with(TestKey.class, this::useTestKey)
                                     .fallthrough((ThrowingConsumer<Object, Exception>) o -> {
                                         throw new Exception(FALLTHROUGH_MESSAGE);
                                     });

        TestKey testKey = new TestKey();
        testKeyMatcher.match(testKey);

        assertNotNull(testKeyMatcher);
        assertTrue(testKey.called);

        AnotherTestKey anotherTestKey = new AnotherTestKey();

        Exception exception = assertThrows(Exception.class, () -> testKeyMatcher.match(anotherTestKey));
        assertNotNull(exception);
        assertEquals(EXPECTED_FULL_FALLTHROUGH_MESSAGE, exception.getMessage());

        ClassMatcher anotherMatcher = ClassMatcher.matcher()
                                                  .with(TestKey.class, this::useTestKey)
                                                  .with(AnotherTestKey.class, this::useAnotherTestKey)
                                                  .fallthrough((ThrowingConsumer<Object, Exception>) o -> {
                                                      throw new Exception(FALLTHROUGH_MESSAGE);
                                                  });

        TestKey testKey2 = new TestKey();
        anotherMatcher.match(anotherTestKey);

        assertNotNull(anotherMatcher);
        assertTrue(anotherTestKey.called);

        anotherMatcher.match(testKey2);
        assertTrue(testKey2.called);
    }

    private boolean useAnotherTestKey(AnotherTestKey anotherTestKey) {
        return anotherTestKey.called = true;
    }

    private boolean useTestKey(TestKey testKey) {
        return testKey.called = true;
    }

    private class AnotherTestKey {
        public boolean called = false;
    }

    private class TestKey {
        public boolean called = false;
    }
}
