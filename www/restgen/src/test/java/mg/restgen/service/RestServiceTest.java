package mg.restgen.service;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
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
    @Ignore
    public void test() {

        TestKey tk = new TestKey();

        restService.apply(tk);

        assertTrue("Value in the test key class should be true.", tk.called);

    }

    @Test
    public void testIsAcceptable() {

        TestKey tk = new TestKey();

        assertTrue("test", restService.isAcceptable(tk));

    }

    public class TestKey {
        public boolean called = false;
    }

}
