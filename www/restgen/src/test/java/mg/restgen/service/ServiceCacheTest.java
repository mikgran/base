package mg.restgen.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        Optional<ServiceInfo> serviceInfoCandidate = ServiceCache.servicesFor(candidateClass);

        assertNotNull(serviceInfoCandidate);
        ServiceInfo serviceInfo = serviceInfoCandidate.get();
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

        Optional<ServiceInfo> serviceInfoCandidate = ServiceCache.servicesFor(candidate.getClass());

        assertNotNull(serviceInfoCandidate);
        ServiceInfo serviceInfo = serviceInfoCandidate.get();
        assertNotNull(serviceInfo.services);
        assertNotNull(serviceInfo.nameRef);
        assertNotNull(serviceInfo.classRef);

        RestService candidateService = serviceInfo.services.get(0);

        assertEquals("the service class should be:", TestService.class, candidateService.getClass());
    }

    @Test
    public void testServicesForString() {

        String nameRef = "TestKey";
        TestKey testKey = new TestKey();
        TestService testService = new TestService();

        ServiceCache.register(testKey.getClass(), testService); // FIXME use getAcceptableTypes instead.
        Optional<ServiceInfo> serviceInfoCandidate = ServiceCache.servicesFor(nameRef);

        assertNotNull(serviceInfoCandidate);
        assertTrue(serviceInfoCandidate.isPresent());
        assertEquals("The nameRef TestKey should produce serviceInfo for TestKey.class.", TestKey.class, serviceInfoCandidate.get().classRef);

        ServiceInfo serviceInfo = serviceInfoCandidate.get();

        serviceInfo.services.stream()
                            .forEach(rs -> rs.apply(testKey, Collections.emptyMap()));

        //.map(o -> o.stream())

        // serviceInfoCandidate.flatMap(o -> o.services.map(Stream::of).orElseGet(Stream::empty));

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
        public List<Class<?>> getAcceptableTypes() { // FIXME: ServiceCache.register(service) -> next iteration should use this method for registering?
            return Arrays.asList(TestKey.class, TestKey2.class);
        }

    }

    public class TestService2 extends TestService {
    }
}
