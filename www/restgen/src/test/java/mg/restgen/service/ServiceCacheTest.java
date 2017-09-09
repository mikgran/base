package mg.restgen.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ServiceCacheTest {

    /*
        - services do actions (interface RestAction.apply()): handle all business logic.
        - base RestService handles the Persistables: the find, findAll, findBy fields, filter by fields, free search etc.
        - TOIMPROVE: far away goal: handle JPAs and hibernates and all other type 'persistables' too
     */

    @Test
    public void testRegister() {

        TestKey candidate = new TestKey();

        ServiceCache.register(candidate, new TestService());

        List<RestService> services = ServiceCache.servicesFor(candidate);
        RestService candidateService = services.get(0);

        assertNotNull(services);
        assertEquals("there should be RestServices: ", 1, services.size());
        assertEquals("the service class should be:", TestService.class, candidateService.getClass());
    }

    public class TestKey {
        public boolean called = false;
    }

    public class TestService implements RestService {

        @Override
        public void apply(Object object) {
        }

        @Override
        public List<Class<?>> getAcceptableTypes() {
            return Arrays.asList(TestKey.class);
        }

    }

}
