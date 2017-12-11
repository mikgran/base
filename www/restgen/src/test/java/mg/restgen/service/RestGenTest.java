package mg.restgen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.rest.CustomAnnotationIntrospector;

public class RestGenTest {

    /*
        - services do actions (interface RestAction.apply(target, parameters)): handle all business logic.
        - base RestService handles the Persistables: the find, findAll, findBy fields, filter by fields, free search etc.
        - TOIMPROVE: far away goal: handle JPAs and hibernates and all other type 'persistables' too
     */

    private SimpleFilterProvider defaultFilterProvider;
    private ObjectMapper mapper;
    private ObjectWriter writer;

    public RestGenTest() {
        initMapper();
        initDefaultFilterProvider();
        initDefaultWriter();
    }
    @Test
    public void testRegisterWithClass() {

        Class<?> candidateClass = TestKey2.class;
        String command = "put";

        TestRestGen.register(candidateClass, new TestService2(), command);

        Optional<ServiceInfo> serviceInfoCandidate = TestRestGen.servicesFor(candidateClass, command);

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

        TestRestGen.register(candidate.getClass(), new TestService(), command);

        Optional<ServiceInfo> serviceInfoCandidate = TestRestGen.servicesFor(candidate.getClass(), "put");

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
        TestService2 testService = new TestService2();

        Optional<ServiceInfo> testKey2ServiceCandidate = TestRestGen.servicesFor(TestKey2.class, putCommand);

        assertNotNull(testKey2ServiceCandidate);
        assertFalse(testKey2ServiceCandidate.isPresent(), "there should not be any services without registeration.");

        TestRestGen.register(testService, putCommand);

        Optional<ServiceInfo> testKey2ServiceCandidate2 = TestRestGen.servicesFor(TestKey2.class, putCommand);

        assertNotNull(testKey2ServiceCandidate2);
        assertTrue(testKey2ServiceCandidate2.isPresent());
        assertEquals(1, testKey2ServiceCandidate2.get().services.size());
        assertEquals(TestService2.class, testKey2ServiceCandidate2.get().services.get(0).getClass());
    }

    // @Disabled
    @Test
    public void testService() {

        String jsonObject = "";
        String putCommand = "put";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("nameRef", "contact2");
        TestService testService = new TestService();

        // TestRestGen.register(testService, putCommand);

        List<ServiceResult> serviceResults;
        try {

            serviceResults = TestRestGen.service(jsonObject, parameters);

            boolean resultOk = serviceResults.stream()
                                             .anyMatch(sr -> sr.statusCode == 200);

            assertTrue(resultOk);

        } catch (ServiceException e) {

            fail("not expecting an exception from the service() call: " + e.getMessage() + ", result: " + e.serviceResult.message);
        }
    }

    @Test
    public void testServicesForStringAndPerformApply() {

        String command = "put";
        String nameRef = "TestKey";
        TestKey testKey = new TestKey();
        TestService testService = new TestService();

        TestRestGen.register(testKey.getClass(), testService, command);
        Optional<ServiceInfo> serviceInfoCandidate = TestRestGen.servicesFor(nameRef, command);

        assertNotNull(serviceInfoCandidate);
        assertTrue(serviceInfoCandidate.isPresent());
        assertEquals(TestKey.class, serviceInfoCandidate.get().classRef, "The nameRef TestKey should produce serviceInfo for TestKey.class.");

        ServiceInfo serviceInfo = serviceInfoCandidate.get();

        assertFalse(testKey.called, "testKey.called ");

        Map<String, Object> emptyMap = Collections.emptyMap();

        serviceInfo.services.stream()
                            .forEach(rs -> rs.apply(testKey, emptyMap));

        assertTrue(testKey.called, "testKey.called ");
    }

    private void initDefaultFilterProvider() {
        defaultFilterProvider = new SimpleFilterProvider();
        defaultFilterProvider.setFailOnUnknownId(false);
    }

    private void initDefaultWriter() {
        writer = mapper.writer(defaultFilterProvider);
    }

    private void initMapper() {
        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }

    class TestKey {
        public boolean called = false;
    }

    class TestKey2 extends TestKey {
    }

    static class TestRestGen extends RestGen {
        protected static ConcurrentHashMap<ServiceKey, ServiceInfo> services = new ConcurrentHashMap<>();
    }

    class TestService extends RestService {

        @Override
        public ServiceResult apply(Object target, Map<String, Object> parameters) {

            if (target instanceof TestKey) {
                TestKey testKeyCandidate = new TestKey().getClass().cast(target);
                testKeyCandidate.called = true;
                return ServiceResult.ok();
            } else if (target instanceof TestKey2) {
                TestKey2 testKeyCandidate = TestKey2.class.cast(target);
                testKeyCandidate.called = true;
                return ServiceResult.ok();
            }

            return ServiceResult.noContent();
        }

        @Override
        public List<Class<?>> getAcceptableTypes() { // FIXME: TestServiceCache.register(service) -> next iteration should use this method for registering?
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
