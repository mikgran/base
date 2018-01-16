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

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.db.Contact2;
import mg.restgen.rest.CustomAnnotationIntrospector;
import mg.util.TestConfig;
import mg.util.db.DBConfig;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.functional.option.Opt;

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

        RestGen restGen = RestGen.init();
        restGen.register(candidateClass, new TestService2(), command);

        Opt<ServiceInfo> serviceInfoCandidate = restGen.servicesFor(candidateClass, command);

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

        RestGen restGen = RestGen.init();
        restGen.register(candidate.getClass(), new TestService(), command);

        Opt<ServiceInfo> serviceInfoCandidate = restGen.servicesFor(candidate.getClass(), "put");

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

        RestGen restGen = RestGen.init();
        Opt<ServiceInfo> testKey2ServiceCandidate = restGen.servicesFor(TestKey2.class, putCommand);

        assertNotNull(testKey2ServiceCandidate);
        assertFalse(testKey2ServiceCandidate.isPresent(), "there should not be any services without registeration.");

        restGen.register(testService, putCommand);

        Opt<ServiceInfo> testKey2ServiceCandidate2 = restGen.servicesFor(TestKey2.class, putCommand);

        assertNotNull(testKey2ServiceCandidate2);
        assertTrue(testKey2ServiceCandidate2.isPresent());
        assertEquals(1, testKey2ServiceCandidate2.get().services.size());
        assertEquals(TestService2.class, testKey2ServiceCandidate2.get().services.get(0).getClass());
    }



    @Test
    public void testService() throws Exception {

        String jsonObject = "";
        String putCommand = "put";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nameref", "contact2");
        parameters.put("command", putCommand);

        RestGen restGen = RestGen.init();
        TestService testService = new TestService();
        restGen.register(Contact2.class, testService, putCommand);

        List<ServiceResult> serviceResults;
        try {

            jsonObject = writer.writeValueAsString(new Contact2(0L, "val1", "val2", "val3"));

            serviceResults = restGen.service(jsonObject, parameters);

            boolean resultOk = serviceResults.stream()
                                             .anyMatch(sr -> sr.statusCode == 200);
            assertTrue(resultOk);

            Long id = serviceResults.stream()
                                    .filter(sr -> sr.payload.getClass()
                                                            .equals(Contact2.class))
                                    .map(sr -> Contact2.class.cast(sr.payload))
                                    .map(Contact2::getId)
                                    .findFirst()
                                    .orElse(0L);

            assertEquals(-1000L, id.longValue());

        } catch (ServiceException e) {

            fail("not expecting an exception from the service() call: " + e.getMessage() + ", result: " + e.serviceResult.message);
        }
    }

    @Test
    public void testServicesForStringAndPerformApply() throws ServiceException {

        String command = "put";
        String nameRef = "TestKey";
        TestKey testKey = new TestKey();
        TestService testService = new TestService();

        RestGen restGen = RestGen.init();
        restGen.register(testKey.getClass(), testService, command);
        Optional<ServiceInfo> serviceInfoCandidate = restGen.servicesFor(nameRef, command);

        assertNotNull(serviceInfoCandidate);
        assertTrue(serviceInfoCandidate.isPresent());
        assertEquals(TestKey.class, serviceInfoCandidate.get().classRef, "The nameRef TestKey should produce serviceInfo for TestKey.class.");

        ServiceInfo serviceInfo = serviceInfoCandidate.get();

        assertFalse(testKey.called, "testKey.called ");

        Map<String, Object> emptyMap = Collections.emptyMap();

        serviceInfo.services.stream()
                            .forEach((ThrowingConsumer<RestService, Exception>) service -> service.apply(testKey, emptyMap));

        assertTrue(testKey.called, "testKey.called ");
    }

    @Test
    public void testServiceWithCrud() throws Exception {

        String jsonObject = "";
        String putCommand = "put";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nameref", "contact2");
        parameters.put("command", putCommand);

        DBConfig dbConfig = new DBConfig(new TestConfig());
        CrudService crudService = new CrudService(dbConfig);
        RestGen restGen = RestGen.init();
        restGen.register(Contact2.class, crudService, putCommand);

        List<ServiceResult> serviceResults;
        try {

            String name = "val1";
            String email = "val2";
            String phone = "val3";
            jsonObject = writer.writeValueAsString(new Contact2(0L, name, email, phone));

            serviceResults = restGen.service(jsonObject, parameters);

            Opt<Integer> statusCode = serviceResults.stream()
                                                    .map(sr -> sr.statusCode)
                                                    .filter(sc -> sc == 201)
                                                    .findFirst()
                                                    .map(Opt::of)
                                                    .get();
            assertEquals(201, (int) statusCode.get());

            Contact2 contact2 = new Contact2().setName(name)
                                              .setEmail(email)
                                              .setPhone(phone);
            contact2.setId(0L);
            contact2.setConnectionAndDB(dbConfig.getConnection());

            Contact2 contact2Candidate = contact2.find();
            assertNotNull(contact2Candidate);
            assertTrue(contact2Candidate.getId() > 0);

        } catch (ServiceException e) {

            fail("not expecting an exception from the service() call: " + e.getMessage() + ", result: " + e.serviceResult.message);
        }
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

    class TestService extends RestService {

        @Override
        public ServiceResult apply(Object target, Map<String, Object> parameters) {

            if (target instanceof TestKey) {
                TestKey testKey = new TestKey().getClass().cast(target);
                testKey.called = true;
                return ServiceResult.ok();
            } else if (target instanceof TestKey2) {
                TestKey2 testKey = TestKey2.class.cast(target);
                testKey.called = true;
                return ServiceResult.ok();
            } else if (target instanceof Contact2) {
                Contact2 contact2 = Contact2.class.cast(target);
                contact2.setId(-1000L);
                return ServiceResult.ok(contact2);
            }

            return ServiceResult.noContent();
        }

        @Override
        public List<Class<?>> getAcceptableTypes() {
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
