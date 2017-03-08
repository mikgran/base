package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// basically map of lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: ServiceCache<RestService>.register
public class ServiceCache {

    private static ConcurrentHashMap<Class<?>, Object> services = new ConcurrentHashMap<>();
    public static <T> void register(Class<?> clazz, T service) {
        validateNotNull("service", service);
        validateNotNull("clazz", clazz);

        // since no static generics
        // include when getting: Class<?> servicesForType, Class<?> serviceType (the services to filter for, Object provided, then all)

        if (services.containsKey(TypeReference.class)) {

            Object object = services.get(TypeReference.class);
        }

        services.put(clazz, service);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> servicesFor(Class<?> clazz) {

        return (List<T>) services.get(clazz);
    }

    public class TypeReference {
        // Empty class just for controlling runtime type with ConcurrentHashMap
    }

}
