package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// basically map of lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: ServiceCache.register(contact, contactRestService) // fail-early: all services need to be instantiated before registered.
public class RestGen {

    protected static ConcurrentHashMap<ServiceKey, ServiceInfo> services = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(RestGen.class.getName());

    public static ConcurrentHashMap<ServiceKey, ServiceInfo> getCache() {
        return services;
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
            // - convert json -> persistable
            // -- mapper
            // - service.apply(persistable)

            String nameref = parameters.get("nameref");
            String command = parameters.get("command");

            ServiceKey serviceKey = ServiceKey.of(nameref,
                                                  command,
                                                  () -> new ServiceException("", getServiceResultForBadQuery(nameref, command)));

                        Optional.ofNullable(services.get(serviceKey))
                                .map(Stream::of)
                                .map(t -> t.)
                                ;

            //            services.entrySet()
            //                    .stream()
            //                    .map(Entry::getKey)
            //                    .filter(serviceKey -> serviceKey.nameRef.toLowerCase()
            //                                                            .equals(nameRef.get().toLowerCase()));
            //                         .findFirst()
            //                         // remove this; change into result badQuery
            //                         .orElseThrow(() -> new ServiceException("", getServiceResultForNoContent(nameRef, command)));

            /// XXX: call the service
            //            si.services.stream()
            //                       .map(service -> service)
            //            ;

            return Arrays.asList(ServiceResult.ok());

        } catch (Exception e) {
            logger.error(e.toString());
            return Arrays.asList(ServiceResult.internalError());
        }

    }

    public static Optional<ServiceInfo> servicesFor(Class<? extends Object> classRef, String command) {
        validateNotNull("classRef", classRef);

        ServiceInfo serviceInfo = null;

        try {
            ServiceKey serviceKey = ServiceKey.of(classRef.getSimpleName(), command);
            serviceInfo = services.get(serviceKey);

        } catch (Exception e) {
        }

        // return serviceInfo != null ? serviceInfo : new ServiceInfo(Collections.emptyList(), null, null);
        return Optional.ofNullable(serviceInfo);
    }

    public static Optional<ServiceInfo> servicesFor(String nameRef, String command) {

        validateNotNullOrEmpty("nameRef", nameRef);
        validateNotNullOrEmpty("command", command);

        Optional<ServiceInfo> classRefCandidate;

        classRefCandidate = services.entrySet()
                                    .stream()
                                    .filter(e -> e.getKey().equals(ServiceKey.of(nameRef, command)))
                                    .map(e -> e.getValue())
                                    .findFirst();

        return classRefCandidate;
    }

    private static void addToServices(Class<? extends Object> classRef, String command, RestService service) {

        validateNotNull("classRef", classRef);
        validateNotNullOrEmpty("command", command);
        validateNotNull("service", service);

        String nameRef = classRef.getSimpleName();
        ServiceKey serviceKey = ServiceKey.of(nameRef, command);

        if (services.containsKey(serviceKey)) {

            ServiceInfo serviceInfo = services.get(serviceKey);
            if (!serviceInfo.services.contains(service)) {
                serviceInfo.services.add(service);
            }

        } else {
            List<RestService> restServices = new ArrayList<>();
            restServices.add(service);

            services.put(ServiceKey.of(nameRef, command),
                         ServiceInfo.of(restServices, classRef, nameRef, command));
        }
    }

    private static ServiceResult getServiceResultForBadQuery(String nameRef, String command) {
        return ServiceResult.badQuery(String.format("nameref: '%s', command: '%s'", nameRef, command));
    }

    private static ServiceResult getServiceResultForNoContent(String nameRef, String command) {
        return ServiceResult.noContent("No content for '" + nameRef + "', '" + command + "'.");
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
