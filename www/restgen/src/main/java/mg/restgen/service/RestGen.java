package mg.restgen.service;

import static java.lang.String.format;
import static mg.util.Common.asInstanceOf;
import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.rest.CustomAnnotationIntrospector;
import mg.util.Common;
import mg.util.db.persist.Persistable;
import mg.util.functional.function.ThrowingBiFunction;
import mg.util.functional.function.ThrowingFunction;
import mg.util.functional.option.Opt;

// basically map of multiple lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: RestGen.register(contact, contactRestService) // fail-early: all services need to be instantiated before registered.
// note on logging: handle logging at the caller level.
public class RestGen {

    private Map<String, ThrowingBiFunction<String, Map<String, Object>, List<ServiceResult>, Exception>> processors = new HashMap<>();
    protected ConcurrentHashMap<ServiceKey, ServiceInfo> serviceInfos = new ConcurrentHashMap<>();

    public static RestGen init() {
        return new RestGen();
    }

    /**
     * No access constructor.
     * Use init() instead.
     */
    private RestGen() {
        initProcessorMap();
    }

    public ConcurrentHashMap<ServiceKey, ServiceInfo> getCache() {
        return serviceInfos;
    }

    public void register(Class<? extends Object> classRef, RestService service, String command) {
        validateNotNull("service", service);
        validateNotNull("classRef", classRef);
        validateNotNull("command", command);

        addToServices(classRef, command, service);
    }

    public void register(RestService service, String command) {
        validateNotNull("service", service);
        validateNotNullOrEmpty("command", command);

        List<Class<? extends Object>> acceptableTypes = service.getAcceptableTypes();

        acceptableTypes.stream()
                       .forEach(classRef -> addToServices(classRef, command, service));
    }

    public List<ServiceResult> service(String jsonObject, Map<String, Object> parameters) throws ServiceException {

        initProcessorMap();

        // default to custom on unknown commands
        // let the custom processor return the client an unknown command response
        Opt<List<ServiceResult>> results;
        results = Opt.of(parameters.get("command"))
                     .ifEmptyThrow(() -> getServiceExceptionForInvalidCommand())
                     .map(command -> processors.get(command))
                     .ifEmpty(() -> processors.get("custom"))
                     .map(processor -> processor.apply(jsonObject, parameters));

        // XXX: finish put, get, update, delete
        return results.getOrElseGet(() -> Collections.emptyList());

    }

    public Opt<ServiceInfo> servicesFor(Class<? extends Object> classRef, String command) {
        validateNotNull("classRef", classRef);
        validateNotNull("command", command);

        ServiceInfo serviceInfo = null;

        try {
            ServiceKey serviceKey = ServiceKey.of(classRef.getSimpleName(), command);
            serviceInfo = serviceInfos.get(serviceKey);

        } catch (Exception e) {
        }
        return Opt.of(serviceInfo);
    }

    public Opt<ServiceInfo> servicesFor(String nameRef, String command) {

        validateNotNullOrEmpty("nameRef", nameRef);
        validateNotNullOrEmpty("command", command);

        ServiceKey serviceKey = ServiceKey.of(nameRef, command);

        ServiceInfo serviceInfo = serviceInfos.get(serviceKey);

        return Opt.of(serviceInfo);
    }

    private void addToServices(Class<? extends Object> classRef, String command, RestService service) {

        validateNotNull("classRef", classRef);
        validateNotNullOrEmpty("command", command);
        validateNotNull("service", service);

        String nameRef = classRef.getSimpleName();
        ServiceKey serviceKey = ServiceKey.of(nameRef, command);

        if (serviceInfos.containsKey(serviceKey)) {

            ServiceInfo serviceInfo = serviceInfos.get(serviceKey);
            serviceInfo.addToServices(service);

        } else {
            List<RestService> restServices = new ArrayList<>();
            restServices.add(service);

            serviceInfos.put(ServiceKey.of(nameRef, command),
                             ServiceInfo.of(restServices, classRef, nameRef, command));
        }
    }

    private ThrowingFunction<RestService, ServiceResult, Exception> applyService(Persistable persistable, Map<String, Object> parameters) {
        return (ThrowingFunction<RestService, ServiceResult, Exception>) service -> service.apply(persistable, parameters);
    }

    private Persistable createPersistableFromClassRef(Opt<ServiceInfo> serviceInfo) throws ServiceException, ReflectiveOperationException {
        return  serviceInfo.map(si -> si.classRef)
                                             .map(cr -> cr.getDeclaredConstructor().newInstance())
                                             .map(asInstanceOf(Persistable.class))
                                             .getOrElseThrow(() -> new ServiceException("Missing classRef."));
    }

    private List<ServiceResult> doGet(String jsonObject, Map<String, Object> parameters) throws Exception {

        ServiceKey serviceKey = getAndValidateServiceKey(parameters);

        Opt<ServiceInfo> serviceInfo = Opt.of(serviceInfos.get(serviceKey)); // Map[key, value], but no inheritance handled.

        Persistable persistable = createPersistableFromClassRef(serviceInfo);

        List<RestService> services = getServices(serviceInfo);

        // For now, every exception breaks the whole chain.
        // It's up to the RestService to decide if an exception should break the chain.
        List<ServiceResult> serviceResults;
        serviceResults = Opt.of(services)
                            .filter(Common::hasContent)
                            .ifEmptyThrow(() -> getServiceExceptionNoServicesDefinedForServiceKey(serviceKey))
                            .get()
                            .stream()
                            .map(applyService(persistable, parameters))
                            .filter(serviceResult -> serviceResult != null)
                            .collect(Collectors.toList());

        return serviceResults;
    }

    private List<ServiceResult> doPut(String jsonObject, Map<String, Object> parameters) throws Exception {

        ServiceKey serviceKey = getAndValidateServiceKey(parameters);

        Opt<ServiceInfo> serviceInfo = Opt.of(serviceInfos.get(serviceKey)); // Map[key, value], but no inheritance handled.

        Persistable persistable = mapJsonToPersistable(jsonObject, serviceInfo);

        List<RestService> services = getServices(serviceInfo);

        // For now, every exception breaks the whole chain.
        // It's up to the RestService to decide if an exception should break the chain.
        List<ServiceResult> serviceResults;
        serviceResults = Opt.of(services)
                            .filter(Common::hasContent)
                            .ifEmptyThrow(() -> getServiceExceptionNoServicesDefinedForServiceKey(serviceKey))
                            .get()
                            .stream()
                            .map(applyService(persistable, parameters))
                            .filter(serviceResult -> serviceResult != null)
                            .collect(Collectors.toList());

        return serviceResults;
    }

    private ServiceKey getAndValidateServiceKey(Map<String, Object> parameters) throws ServiceException {
        String nameref = Opt.of(parameters.get("nameref"))
                            .map(Object::toString)
                            .map(s -> (s.endsWith("s") ? s.substring(0, s.length() - 1) : s)) // TOIMPROVE: plural handling
                            .get();
        String command = Opt.of(parameters.get("command"))
                            .map(Object::toString)
                            .get();

        ServiceKey serviceKey = ServiceKey.of(nameref, command, getMissingParametersExceptionSupplier(nameref, command));
        return serviceKey;
    }

    private List<RestService> getApplicableGeneralServices(Opt<ServiceInfo> serviceInfo) {

        Class<?> classRef = serviceInfo.map(si -> si.classRef).get();

        return serviceInfo.map(si -> si.generalServices)
                          .getOrElseGet(() -> Collections.emptyList())
                          .stream()
                          .filter(genService -> genService.getAcceptableTypes()
                                                          .stream()
                                                          .anyMatch(classRef::isAssignableFrom))
                          .collect(Collectors.toList());
    }

    private ObjectMapper getJsonToObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return mapper;
    }

    private Supplier<ServiceException> getMissingParametersExceptionSupplier(String nameref, String command) {
        return () -> new ServiceException("The nameref or the command missing.", getServiceResultForBadQuery(nameref, command));
    }

    private ServiceException getServiceExceptionForInvalidCommand() {
        return new ServiceException("Invalid command", ServiceResult.internalError("No command provided."));
    }

    private ServiceException getServiceExceptionForInvalidJSon() {
        return new ServiceException("Unable to map json to a Persistable.", ServiceResult.badQuery("provided json can not be mapped to an Persistable."));
    }

    private ServiceException getServiceExceptionNoServicesDefinedForServiceKey(ServiceKey serviceKey) {
        return new ServiceException("no services defined for command.", ServiceResult.ok("No services defined for: " + serviceKey));
    }

    private ServiceResult getServiceResultForBadQuery(String nameRef, String command) {
        return ServiceResult.badQuery(format("nameref: '%s', command: '%s'.", nameRef, command));
    }

    private List<RestService> getServices(Opt<ServiceInfo> serviceInfo) {

        List<RestService> applicableGeneralServices = getApplicableGeneralServices(serviceInfo);

        List<RestService> services = serviceInfo.map(si -> si.services)
                                                .getOrElseGet(() -> Collections.emptyList());

        return Stream.concat(applicableGeneralServices.stream(), services.stream())
                     .distinct()
                     .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    private SimpleFilterProvider getSimpleFilterProvider() {
        SimpleFilterProvider defaultFilterProvider = new SimpleFilterProvider();
        defaultFilterProvider.setFailOnUnknownId(false);
        return defaultFilterProvider;
    }

    private void initProcessorMap() {

        // start with at least the crud operations
        if (processors.size() == 0) {

            // crud operations
            processors.put("put", this::doPut);
            processors.put("get", this::doGet); // XXX: add all missing processors.
            // processors.put("update", RestGen::doUpdate);
            // processors.put("delete", RestGen::doDelete);

            // custom operations -> handle everything else but get, put, update, delete
            // XXX: use query parameters for custom commands or use rest pathing?
            // processors.put("custom", RestGen::doCustom)
        }
    }

    private Persistable mapJsonToPersistable(String jsonObj, Opt<ServiceInfo> serviceInfo) throws ServiceException, Exception {

        String jsonObject = Opt.of(jsonObj)
                               .ifEmptyThrow(() -> getServiceExceptionForInvalidJSon())
                               .get();

        ObjectMapper mapper = getJsonToObjectMapper();
        // SimpleFilterProvider defaultFilterProvider = getSimpleFilterProvider();
        // ObjectWriter writer = mapper.writer(defaultFilterProvider);

        // throw an exception, because we can not do anything without the actual Persistable -> break the chain
        return serviceInfo.map(si -> mapper.readValue(jsonObject, si.classRef))
                          .map(asInstanceOf(Persistable.class))
                          .ifEmptyThrow(() -> getServiceExceptionForInvalidJSon())
                          .get();
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
