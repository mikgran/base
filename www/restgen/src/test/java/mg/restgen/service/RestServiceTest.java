package mg.restgen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class RestServiceTest {

    private TestKey tk = new TestKey();
    private AnotherTestKey atk = new AnotherTestKey();

    RestService restService = new RestService() {

        @Override
        public void apply(Object object) {

            boolean isAcceptable = isAcceptable(object);

            if (isAcceptable) {

                TestKey tk = (TestKey) object;
                tk.called = true;
            }
        }

        @Override
        public List<Class<?>> getAcceptableTypes() {

            return Arrays.asList(TestKey.class);
        }

    };



    @Test
    public void testApply() {

        TestKey tk = new TestKey();

        assertFalse(tk.called, "Value in the test key before calling should be false.");

        restService.apply(tk);

        assertTrue(tk.called, "Value in the test key class should be true.");
    }

    @Test
    public void testIsAcceptable() {

        assertTrue(restService.isAcceptable(tk), "TestKey class should be acceptable.");
        assertFalse(restService.isAcceptable(atk), "AnotherTestKey class should not be accepted.");

    }

    @Test
    public void testIsAcceptableWithNullParameter() {

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {

            Assertions.assertNotNull(restService);
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
