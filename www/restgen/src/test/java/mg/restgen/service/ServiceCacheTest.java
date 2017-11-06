package mg.restgen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

public class ServiceCacheTest {

    /*
        - services do actions (interface RestAction.apply()): handle all business logic.
        - base RestService handles the Persistables: the find, findAll, findBy fields, filter by fields, free search etc.
        - TOIMPROVE: far away goal: handle JPAs and hibernates and all other type 'persistables' too
     */


    @Test
    public void testRegisterWithClass() {

        ConcurrentHashMap<ServiceKey, ServiceInfo> cache = ServiceCache.getCache();

        Class<?> candidateClass = TestKey2.class;
        String command = "put";

        ServiceCache.register(candidateClass, new TestService2(), command);

        Optional<ServiceInfo> serviceInfoCandidate = ServiceCache.servicesFor(candidateClass, command);

        assertNotNull(serviceInfoCandidate);
        assertTrue(serviceInfoCandidate.isPresent());
        ServiceInfo serviceInfo = serviceInfoCandidate.get();
        assertNotNull(serviceInfo.services);
        assertNotNull(serviceInfo.nameRef);
        assertNotNull(serviceInfo.classRef);
        assertTrue(serviceInfo.services.size() >= 1, "there should be RestServices: ");

        RestService candidateService = serviceInfo.services.get(0);

        assertEquals(TestService2.class, candidateService.getClass(), "the service class should be: ");
        assertEquals(TestKey2.class, serviceInfo.classRef, "the service classRef should be: ");
        assertEquals("TestKey2", serviceInfo.nameRef, "the service nameRef should be: ");
    }

    @Test
    public void testRegisterWithObject() {

        TestKey candidate = new TestKey();
        String command = "put";

        ServiceCache.register(candidate.getClass(), new TestService(), command);

        Optional<ServiceInfo> serviceInfoCandidate = ServiceCache.servicesFor(candidate.getClass(), "put");

        assertNotNull(serviceInfoCandidate);
        ServiceInfo serviceInfo = serviceInfoCandidate.get();
        assertNotNull(serviceInfo.services);
        assertNotNull(serviceInfo.nameRef);
        assertNotNull(serviceInfo.classRef);

        RestService candidateService = serviceInfo.services.get(0);

        assertEquals(TestService.class, candidateService.getClass(), "the service class should be:");
    }

    @Test
    public void testRegisterWithoutClassOrObject() {

        String putCommand = "put";
        // TestKey2 testKey2 = new TestKey2();
        TestService2 testService = new TestService2();

        Optional<ServiceInfo> testKey2ServiceCandidate = ServiceCache.servicesFor(TestKey2.class, putCommand);

        assertNotNull(testKey2ServiceCandidate);
        assertFalse(testKey2ServiceCandidate.isPresent(), "there should not be any services without registeration.");

        ServiceCache.register(testService, putCommand);

        Optional<ServiceInfo> testKey2ServiceCandidate2 = ServiceCache.servicesFor(TestKey2.class, putCommand);

        assertNotNull(testKey2ServiceCandidate2);
        assertTrue(testKey2ServiceCandidate2.isPresent());
        assertEquals(1, testKey2ServiceCandidate2.get().services.size());
        assertEquals(TestService2.class, testKey2ServiceCandidate2.get().services.get(0).getClass());
    }

    @Test
    public void testServicesForServiceKey() {
        // FIXME !
    }

    @Test
    public void testServicesForStringAndPerformApply() {

        String command = "put";
        String nameRef = "TestKey";
        TestKey testKey = new TestKey();
        TestService testService = new TestService();

        ServiceCache.register(testKey.getClass(), testService, command);
        Optional<ServiceInfo> serviceInfoCandidate = ServiceCache.servicesFor(nameRef, command);

        assertNotNull(serviceInfoCandidate);
        assertTrue(serviceInfoCandidate.isPresent());
        assertEquals(TestKey.class, serviceInfoCandidate.get().classRef, "The nameRef TestKey should produce serviceInfo for TestKey.class.");

        ServiceInfo serviceInfo = serviceInfoCandidate.get();

        assertFalse(testKey.called, "testKey.called ");

        serviceInfo.services.stream()
                            .forEach(rs -> rs.apply(testKey, Collections.emptyMap()));

        assertTrue(testKey.called, "testKey.called ");
    }

    class TestKey {
        public boolean called = false;
    }

    class TestKey2 extends TestKey {
    }

    class TestService extends RestService {

        @Override
        public void apply(Object target, Map<String, Object> parameters) {

            if (target instanceof TestKey) {
                TestKey testKeyCandidate = new TestKey().getClass().cast(target);
                testKeyCandidate.called = true;
            } else if (target instanceof TestKey2) {
                TestKey2 testKeyCandidate = TestKey2.class.cast(target);
                testKeyCandidate.called = true;
            }
        }

        @Override
        public List<Class<?>> getAcceptableTypes() { // FIXME: ServiceCache.register(service) -> next iteration should use this method for registering?
            return Arrays.asList(TestKey.class, TestKey2.class);
        }

    }

    class TestService2 extends TestService {
        @Override
        public List<Class<?>> getAcceptableTypes() {

            return Arrays.asList(TestKey2.class);
        }
    }

}
