package mg.restgen.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class RestServiceTest {

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

        assertFalse("Value in the test key before calling should be false.", tk.called);

        restService.apply(tk);

        assertTrue("Value in the test key class should be true.", tk.called);
    }

    @Test
    public void testIsAcceptable() {

        TestKey tk = new TestKey();
        AnotherTestKey atk = new AnotherTestKey();

        assertTrue("TestKey class should be acceptable.", restService.isAcceptable(tk));
        assertFalse("AnotherTestKey class should not be accepted.", restService.isAcceptable(atk));
    }

    public class AnotherTestKey {
        public boolean called = false;
    }

    public class TestKey {
        public boolean called = false;
    }

}
