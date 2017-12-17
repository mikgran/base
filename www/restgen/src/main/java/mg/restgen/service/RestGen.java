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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mg.restgen.rest.CustomAnnotationIntrospector;
import mg.util.Common;
import mg.util.db.persist.Persistable;
import mg.util.functional.function.ThrowingFunction;

// basically map of lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: ServiceCache.register(contact, contactRestService) // fail-early: all services need to be instantiated before registered.
public class RestGen {

    protected static ConcurrentHashMap<ServiceKey, ServiceInfo> serviceInfos = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(RestGen.class.getName());

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

        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new CustomAnnotationIntrospector());
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

        SimpleFilterProvider defaultFilterProvider = new SimpleFilterProvider();
        defaultFilterProvider.setFailOnUnknownId(false);

        ObjectWriter writer = mapper.writer(defaultFilterProvider);

        try {
            // - convert json -> persistable
            // -- get / put / delete / update
            // -- mapper
            // - service.apply(persistable)

            String nameref = parameters.get("nameref");
            String command = parameters.get("command");

            // case user provided unfitting service key
            ServiceKey serviceKey = ServiceKey.of(nameref,
                                                  command,
                                                  getMissingParametersExceptionSupplier(nameref, command));

            Optional<ServiceInfo> serviceInfo = Optional.ofNullable(serviceInfos.get(serviceKey));
            Optional<String> jsonObj = Optional.ofNullable(jsonObject);

            // for put, get has no structure.
            Persistable target;
            target = serviceInfo.filter(si -> jsonObj.isPresent())
                                .map((ThrowingFunction<ServiceInfo, Object, Exception>) si -> mapper.readValue(jsonObj.get(), si.classRef))
                                .map(asInstanceOf(Persistable.class))
                                .orElseThrow(() -> new ServiceException("Unable to map json -> persistable.", ServiceResult.internalError()));

            Map<String, Object> serviceParameters = new HashMap<>();

            // case service key defined, but no services present
            List<ServiceResult> serviceResults;
            serviceResults = serviceInfo.map(si -> si.services)
                                        .filter(Common::hasContent)
                                        .orElseThrow(getNoServicesDefinedExceptionSupplier(nameref, command))
                                        .stream()
                                        .map(service -> service.apply(target, serviceParameters))
                                        .collect(Collectors.toList());

            return serviceResults;

        } catch (Exception e) {
            logger.error(e.getMessage());
            // e.printStackTrace();
            return Arrays.asList(ServiceResult.internalError());
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

    private static Supplier<ServiceException> getMissingParametersExceptionSupplier(String nameref, String command) {
        return () -> new ServiceException("", getServiceResultForBadQuery(nameref, command));
    }

    private static Supplier<? extends ServiceException> getNoServicesDefinedExceptionSupplier(String nameref, String command) {
        return () -> new ServiceException("no services defined for: " + nameref,
                                          getServiceResultForNoServicesDefined(nameref, command));
    }

    private static ServiceResult getServiceResultForBadQuery(String nameRef, String command) {
        return ServiceResult.badQuery(format("nameref: '%s', command: '%s'.", nameRef, command));
    }

    private static ServiceResult getServiceResultForNoContent(String nameRef, String command) {
        return ServiceResult.noContent(format("No content for nameref: '%s', command: '%s'.", nameRef, command));
    }

    private static ServiceResult getServiceResultForNoServicesDefined(String nameRef, String command) {
        return ServiceResult.ok("", format("No services defined for nameref: '%s', command: '%s'.", nameRef, command));
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
