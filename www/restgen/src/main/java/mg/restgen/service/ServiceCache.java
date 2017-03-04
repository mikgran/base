package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// basically map of lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: ServiceCache<RestService>.register
public class ServiceCache {

    private static ConcurrentHashMap<Class<?>, Object> services = new ConcurrentHashMap<>();

    public static <T> List<T> of(Class<?> class1) {
        return null; //services.get;
    }

    public static <T> void register(Class<?> clazz, T service) {
        validateNotNull("service", service);
        validateNotNull("clazz", clazz);


        services.put(clazz, service);
    }



}



