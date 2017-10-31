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

    protected static ConcurrentHashMap<String, ServiceInfo> services = new ConcurrentHashMap<>();

    public static void register(Class<? extends Object> classRef, RestService service) {
        validateNotNull("service", service);
        validateNotNull("classRef", classRef);

        addToServices(classRef, service);
    }

    public static void register(RestService service) {

        validateNotNull("service", service);

        List<Class<? extends Object>> acceptableTypes = service.getAcceptableTypes();

        acceptableTypes.stream()
                       .forEach(classRef -> addToServices(classRef, service));

    }

    public static Optional<ServiceInfo> servicesFor(Class<? extends Object> classRef) {
        validateNotNull("classRef", classRef);

        ServiceInfo serviceInfo = null;

        try {

            serviceInfo = services.get(classRef.getSimpleName());

        } catch (Exception e) {
        }

        // return serviceInfo != null ? serviceInfo : new ServiceInfo(Collections.emptyList(), null, null);
        return Optional.of(serviceInfo);
    }

    public static Optional<ServiceInfo> servicesFor(String nameRef) {

        validateNotNullOrEmpty("nameRef", nameRef);

        Optional<ServiceInfo> classRefCandidate;

        classRefCandidate = services.entrySet()
                                    .stream()
                                    .filter(e -> e.getKey().equals(nameRef))
                                    .map(e -> e.getValue())
                                    .findFirst();

        return classRefCandidate;
    }

    private static void addToServices(Class<? extends Object> classRef, RestService service) {

        String nameRef = classRef.getSimpleName();

        if (services.containsKey(nameRef)) {

            ServiceInfo serviceInfo = services.get(nameRef);

            serviceInfo.services.add(service);

        } else {
            List<RestService> restServices = new ArrayList<>();
            restServices.add(service);

            services.put(nameRef, new ServiceInfo(restServices, classRef, nameRef));
        }
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
