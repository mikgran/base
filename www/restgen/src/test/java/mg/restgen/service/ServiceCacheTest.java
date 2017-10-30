package mg.restgen.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ServiceCacheTest {

    /*
        - services do actions (interface RestAction.apply()): handle all business logic.
        - base RestService handles the Persistables: the find, findAll, findBy fields, filter by fields, free search etc.
        - TOIMPROVE: far away goal: handle JPAs and hibernates and all other type 'persistables' too
     */

    @Test
    public void testRegisterWithClass() {

        Class<?> candidateClass = TestKey2.class;

        ServiceCache.register(candidateClass, new TestService2());

        ServiceInfo serviceInfo = ServiceCache.servicesFor(candidateClass);

        assertNotNull(serviceInfo);
        assertNotNull(serviceInfo.services);
        assertNotNull(serviceInfo.nameRef);
        assertNotNull(serviceInfo.classRef);
        assertEquals("there should be RestServices: ", 1, serviceInfo.services.size());

        RestService candidateService = serviceInfo.services.get(0);

        assertEquals("the service class should be: ", TestService2.class, candidateService.getClass());
        assertEquals("the service classRef should be: ", TestKey2.class, serviceInfo.classRef);
        assertEquals("the service nameRef should be: ", "TestKey2", serviceInfo.nameRef);
    }

    @Test
    public void testRegisterWithObject() {

        TestKey candidate = new TestKey();

        ServiceCache.register(candidate.getClass(), new TestService());

        ServiceInfo serviceInfo = ServiceCache.servicesFor(candidate.getClass());

        assertNotNull(serviceInfo);
        assertNotNull(serviceInfo.services);
        assertNotNull(serviceInfo.nameRef);
        assertNotNull(serviceInfo.classRef);

        RestService candidateService = serviceInfo.services.get(0);

        assertEquals("the service class should be:", TestService.class, candidateService.getClass());
    }

    public class TestKey {
        public boolean called = false;
    }

    public class TestKey2 extends TestKey {
    }

    public class TestService extends RestService {

        @Override
        public void apply(Object target, Map<String, String> parameters) {
        }

        @Override
        public List<Class<?>> getAcceptableTypes() {
            return Arrays.asList(this.getClass());
        }

    }

    public class TestService2 extends TestService {

        @Override
        public List<Class<?>> getAcceptableTypes() {
            return Arrays.asList(this.getClass());
        }
    }

}
