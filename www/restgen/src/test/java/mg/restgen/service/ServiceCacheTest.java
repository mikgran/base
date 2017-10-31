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

    private static boolean isServiceCacheInitDone = false;

    /*
        - services do actions (interface RestAction.apply()): handle all business logic.
        - base RestService handles the Persistables: the find, findAll, findBy fields, filter by fields, free search etc.
        - TOIMPROVE: far away goal: handle JPAs and hibernates and all other type 'persistables' too
     */

    public synchronized void initTestServiceCache() {
        if (isServiceCacheInitDone) {
            return;
        }
        // ensure called at least once
        @SuppressWarnings("unused")
        TestServiceCache testServiceCache = new TestServiceCache();
        isServiceCacheInitDone = true;
    }

    @Test
    public void testRegisterWithClass() {

        initTestServiceCache();

        Class<?> candidateClass = TestKey2.class;

        TestServiceCache.register(candidateClass, new TestService2());

        Optional<ServiceInfo> serviceInfoCandidate = TestServiceCache.servicesFor(candidateClass);

        assertNotNull(serviceInfoCandidate);
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

        TestServiceCache.register(candidate.getClass(), new TestService());

        Optional<ServiceInfo> serviceInfoCandidate = TestServiceCache.servicesFor(candidate.getClass());

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

        initTestServiceCache();

        TestKey2 testKey2 = new TestKey2();
        TestService2 testService = new TestService2();

        TestServiceCache.register(testService);
    }

    @Test
    public void testServicesForStringAndPerformApply() {

        initTestServiceCache();

        String nameRef = "TestKey";
        TestKey testKey = new TestKey();
        TestService testService = new TestService();

        TestServiceCache.register(testKey.getClass(), testService); // FIXME use getAcceptableTypes instead.
        Optional<ServiceInfo> serviceInfoCandidate = TestServiceCache.servicesFor(nameRef);

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
        public void apply(Object target, Map<String, String> parameters) {

            if (target instanceof TestKey) {
                TestKey testKeyCandidate = TestKey.class.cast(target);
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
    }

    class TestServiceCache extends ServiceCache {

        public TestServiceCache() {
            services = new ConcurrentHashMap<>(); // replace the existing ConcurrenHashMap
        }


    }

}
