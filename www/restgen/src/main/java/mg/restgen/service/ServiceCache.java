package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// basically map of lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: ServiceCache.register(contact, contactRestService) // fail-early: all services need to be instantiated before registered.
public class ServiceCache {

    protected static ConcurrentHashMap<ServiceKey, ServiceInfo> services = new ConcurrentHashMap<>();

    public static void register(Class<? extends Object> classRef, RestService service) {
        validateNotNull("service", service);
        validateNotNull("classRef", classRef);

        addToServices(classRef, service);
    }

    public static void register(RestService service, String command) {

        validateNotNull("service", service);
        validateNotNullOrEmpty("command", command);

        List<Class<? extends Object>> acceptableTypes = service.getAcceptableTypes();

        acceptableTypes.stream()
                       .forEach(classRef -> addToServices(classRef, command, service));

    }

    public static Optional<ServiceInfo> servicesFor(Class<? extends Object> classRef) {
        validateNotNull("classRef", classRef);

        ServiceInfo serviceInfo = null;

        try {

            serviceInfo = services.get(classRef.getSimpleName());

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

        String nameRef = classRef.getSimpleName();

        ServiceKey serviceKey = ServiceKey.of(nameRef, command);

        if (services.containsKey(serviceKey)) {

            ServiceInfo serviceInfo = services.get(serviceKey);
            serviceInfo.services.add(service);

        } else {
            List<RestService> restServices = new ArrayList<>();
            restServices.add(service);

            services.put(ServiceKey.of(nameRef, command), new ServiceInfo(restServices, classRef, nameRef));
        }
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
