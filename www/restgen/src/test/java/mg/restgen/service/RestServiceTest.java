package mg.restgen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class RestServiceTest {

    private TestKey tk = new TestKey();
    private AnotherTestKey atk = new AnotherTestKey();

    RestService restService = new RestService() {

        @Override
        public ServiceResult apply(Object target, Map<String, Object> parameters) {

            Optional<ServiceResult> result;

            result = Optional.ofNullable(target)
                             .filter(TestKey.class::isInstance)
                             .map(TestKey.class::cast)
                             .map(tk -> {
                                 tk.called = true;
                                 return tk;
                             })
                             .map(tk -> ServiceResult.ok());

            return result.orElseGet(() -> ServiceResult.badQuery("Target not acceptable."));
        }

        @Override
        public List<Class<?>> getAcceptableTypes() {

            return Arrays.asList(TestKey.class);
        }

    };

    @Test
    public void testApplyNoParameters() throws Exception {

        TestKey tk = new TestKey();

        assertFalse(tk.called, "Value in the test key before calling should be false.");

        restService.apply(tk, Collections.emptyMap());

        assertTrue(tk.called, "Value in the test key class should be true.");
    }

    @Test
    public void testApplyWithParameters() throws Exception {

        TestKey tk = new TestKey();

        assertFalse(tk.called, "Value in the test key before calling should be false");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("command", "put");

        restService.apply(tk, parameters);

        assertTrue(tk.called, "Value in the test key class should be true.");
    }

    @Test
    public void testIsAcceptable() {

        assertTrue(restService.isAcceptable(tk), "TestKey class should be acceptable.");
        assertFalse(restService.isAcceptable(atk), "AnotherTestKey class should not be accepted.");
    }

    @Test
    public void testIsAcceptableWithNullParameter() {
        {
            Throwable exception = assertThrows(IllegalArgumentException.class, () -> {

                assertNotNull(restService);
                restService.isAcceptable((Object) null);
            });

            assertEquals("o can not be null.", exception.getMessage());
        }
        {
            Throwable exception = assertThrows(IllegalArgumentException.class, () -> {

                assertNotNull(restService);
                restService.isAcceptable((Class<?>) null);
            });

            assertEquals("cls can not be null.", exception.getMessage());
        }

    }

    public class AnotherTestKey {
        public boolean called = false;
    }

    public class TestKey {
        public boolean called = false;
    }
}
