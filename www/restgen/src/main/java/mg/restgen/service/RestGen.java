package mg.restgen.service;

import static java.lang.String.format;
import static mg.util.Common.asInstanceOf;
import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.rest.CustomAnnotationIntrospector;
import mg.util.db.persist.Persistable;
import mg.util.functional.function.ThrowingBiFunction;
import mg.util.functional.function.ThrowingFunction;
import mg.util.functional.option.Opt;
import mg.util.functional.supplier.ThrowingSupplier;

// basically map of lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: ServiceCache.register(contact, contactRestService) // fail-early: all services need to be instantiated before registered.
public class RestGen {

    protected static ConcurrentHashMap<ServiceKey, ServiceInfo> serviceInfos = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(RestGen.class.getName());
    private static Map<String, ThrowingBiFunction<String, Map<String, String>, List<ServiceResult>, Exception>> processors = new HashMap<>();

    public static ConcurrentHashMap<ServiceKey, ServiceInfo> getCache() {
        return serviceInfos;
    }

    public static void register(Class<? extends Object> classRef, RestService service, String command) {
        validateNotNull("service", service);
        validateNotNull("classRef", classRef);
        validateNotNull("command", command);

        addToServices(classRef, command, service);
    }

    public static void register(RestService service, String command) {
        validateNotNull("service", service);
        validateNotNullOrEmpty("command", command);

        List<Class<? extends Object>> acceptableTypes = service.getAcceptableTypes();

        acceptableTypes.stream()
                       .forEach(classRef -> addToServices(classRef, command, service));
    }

    public static List<ServiceResult> service(String jsonObject, Map<String, String> parameters) throws ServiceException {

        try {
            initializeProcessorMap();

            Opt.of(parameters.get("command"))
               .ifMissing(getInvalidCommandExceptionSupplier())
               .map(command -> processors.get(command))
               .map(processor -> processor.apply(jsonObject, parameters));

            // XXX: finish put, get, update, delete

            return null;

        } catch (Exception e) {

            logger.error(e.getMessage());
            return Arrays.asList(ServiceResult.internalError(e.getMessage()));
        }

    }

    public static Optional<ServiceInfo> servicesFor(Class<? extends Object> classRef, String command) {
        validateNotNull("classRef", classRef);
        validateNotNull("command", command);

        ServiceInfo serviceInfo = null;

        try {
            ServiceKey serviceKey = ServiceKey.of(classRef.getSimpleName(), command);
            serviceInfo = serviceInfos.get(serviceKey);

        } catch (Exception e) {
        }

        // return serviceInfo != null ? serviceInfo : new ServiceInfo(Collections.emptyList(), null, null);
        return Optional.ofNullable(serviceInfo);
    }

    public static Optional<ServiceInfo> servicesFor(String nameRef, String command) {

        validateNotNullOrEmpty("nameRef", nameRef);
        validateNotNullOrEmpty("command", command);

        ServiceKey serviceKey = ServiceKey.of(nameRef, command);

        ServiceInfo serviceInfo = serviceInfos.get(serviceKey);

        return Optional.ofNullable(serviceInfo);
    }

    private static void addToServices(Class<? extends Object> classRef, String command, RestService service) {

        validateNotNull("classRef", classRef);
        validateNotNullOrEmpty("command", command);
        validateNotNull("service", service);

        String nameRef = classRef.getSimpleName();
        ServiceKey serviceKey = ServiceKey.of(nameRef, command);

        if (serviceInfos.containsKey(serviceKey)) {

            ServiceInfo serviceInfo = serviceInfos.get(serviceKey);
            if (!serviceInfo.services.contains(service)) {
                serviceInfo.services.add(service);
            }

        } else {
            List<RestService> restServices = new ArrayList<>();
            restServices.add(service);

            serviceInfos.put(ServiceKey.of(nameRef, command),
                             ServiceInfo.of(restServices, classRef, nameRef, command));
        }
    }

    private static List<ServiceResult> doPut(String jsonObject, Map<String, String> parameters) throws Exception {
        String nameref = parameters.get("nameref");
        String command = parameters.get("command");

        // case user provided unfitting service key

        ServiceKey serviceKey = ServiceKey.of(nameref, command, getMissingParametersExceptionSupplier(nameref, command));

        Opt<ServiceInfo> serviceInfo = Opt.of(serviceInfos.get(serviceKey));
        Opt<String> jsonObj = Opt.of(jsonObject)
                                 .ifMissing(getMissingJsonExceptionSupplier());

        // case put, validate, map json -> Persistable

        ObjectMapper mapper = getJsonToObjectMapper();
        SimpleFilterProvider defaultFilterProvider = getSimpleFilterProvider();
        ObjectWriter writer = mapper.writer(defaultFilterProvider);

        Persistable target;

        serviceInfo.map(getJsonToclassRefPersistableMapper(jsonObj, mapper))
                   .map(asInstanceOf(Persistable.class))
                   .ifMissing(getUnableToMapJsonExSupplier())
        ;

        //        Persistable target;
        //        target = serviceInfo.map((ThrowingFunction<ServiceInfo, Object, Exception>) si -> mapper.readValue(jsonObj.get(), si.classRef))
        //                            .map(asInstanceOf(Persistable.class))
        //                            .orElseThrow(() -> new ServiceException("Unable to map json -> persistable.", ServiceResult.badQuery("Invalid json.")));

        //        Map<String, Object> serviceParameters = new HashMap<>();
        //        // apply service, handle error situations.
        List<ServiceResult> serviceResults = null;
        //        serviceResults = serviceInfo.map(si -> si.services)
        //                                    .filter(Common::hasContent)
        //                                    .orElseThrow(getNoServicesDefinedExceptionSupplier(nameref, command))
        //                                    .stream()
        //                                    .map(service -> service.apply(target, serviceParameters))
        //                                    .collect(Collectors.toList());

        return serviceResults;
    }

    private static ThrowingSupplier<String, ServiceException> getInvalidCommandExceptionSupplier() {
        return () -> {
               throw new ServiceException("Invalid command", ServiceResult.badQuery("Invalid command."));
           };
    }

    private static ThrowingFunction<ServiceInfo, Object, Exception> getJsonToclassRefPersistableMapper(Opt<String> jsonObj, ObjectMapper mapper) {
        return serviceInfo -> mapper.readValue(jsonObj.get(), serviceInfo.classRef);

    }

    private static ObjectMapper getJsonToObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return mapper;
    }

    private static ThrowingSupplier<String, ServiceException> getMissingJsonExceptionSupplier() {
        return () -> {
             throw new ServiceException("RestGen expects a non empty json for put command.", ServiceResult.badQuery("invalid json."));
         };
    }

    private static Supplier<ServiceException> getMissingParametersExceptionSupplier(String nameref, String command) {
        return () -> new ServiceException("", getServiceResultForBadQuery(nameref, command));
    }

    private static ServiceResult getServiceResultForBadQuery(String nameRef, String command) {
        return ServiceResult.badQuery(format("nameref: '%s', command: '%s'.", nameRef, command));
    }

    private static SimpleFilterProvider getSimpleFilterProvider() {
        SimpleFilterProvider defaultFilterProvider = new SimpleFilterProvider();
        defaultFilterProvider.setFailOnUnknownId(false);
        return defaultFilterProvider;
    }

    private static ThrowingSupplier<Persistable, Exception> getUnableToMapJsonExSupplier() {
        return () -> {
            throw new ServiceException("", ServiceResult.badQuery("provided json can not be mapped to an Persistable."));
        };
    }

    private static void initializeProcessorMap() {
        if (processors.size() == 0) {
            processors.put("put", RestGen::doPut);
            // processors.put("get", RestGen::doGet);
            // processors.put("update", RestGen::doUpdate);
            // processors.put("delete", RestGen::doDelete);
        }
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
