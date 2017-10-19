package mg.restgen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class RestServiceTest {

    private TestKey tk = new TestKey();
    private AnotherTestKey atk = new AnotherTestKey();

    RestService restService = new RestService() {

        @Override
        public void apply(Object target, Set<String> parameters) {

            this.parameters.addAll(parameters);
            boolean isAcceptable = isAcceptable(target);

            if (isAcceptable) {

                TestKey tk = (TestKey) target;
                tk.called = true;
            }
        }

        @Override
        public List<Class<?>> getAcceptableTypes() {

            return Arrays.asList(TestKey.class);
        }

    };

    @Test
    public void testApplyNoParameters() {

        TestKey tk = new TestKey();

        assertFalse(tk.called, "Value in the test key before calling should be false.");

        restService.apply(tk, Collections.emptySet());

        assertTrue(tk.called, "Value in the test key class should be true.");
        assertEquals(0, restService.getParameters().size(), "There should be no parameters.");
    }

    @Test
    public void testApplyWithParameters() {

        TestKey tk = new TestKey();

        assertFalse(tk.called, "Value in the test key before calling should be false");

        Set<String> parameters = new HashSet<>();
        parameters.add("put");

        restService.apply(tk, parameters);

        assertTrue(tk.called, "Value in the test key class should be true.");
        assertTrue(restService.getParameters().size() > 0, "There should be at least one parameter within restService.");
    }

    @Test
    public void testIsAcceptable() {

        assertTrue(restService.isAcceptable(tk), "TestKey class should be acceptable.");
        assertFalse(restService.isAcceptable(atk), "AnotherTestKey class should not be accepted.");
    }

    @Test
    public void testIsAcceptableWithNullParameter() {

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {

            assertNotNull(restService);
            restService.isAcceptable(null);
        });

        assertEquals("o can not be null.", exception.getMessage());

    }

    public class AnotherTestKey {
        public boolean called = false;
    }

    public class TestKey {
        public boolean called = false;
    }
}
