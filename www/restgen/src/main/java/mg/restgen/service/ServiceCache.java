package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// basically map of lists of instantiated services.
// should be registered at the start of the program in order to fail-fast in case of missing resources/whatnot
// usage: ServiceCache.register(contact, contactRestService) // fail-early: all services need to be instantiated before registered.
public class ServiceCache {

    private static ConcurrentHashMap<Object, List<RestService>> services = new ConcurrentHashMap<>();

    public static void register(Object o, RestService service) {
        validateNotNull("service", service);
        validateNotNull("clazz", o);

        // since no static generics
        // include when getting: Class<?> servicesForType, Class<?> serviceType (the services to filter for, Object provided, then all)

        if (services.containsKey(o)) {

            List<RestService> servicesForO = services.get(o);

            servicesForO.add(service);

        } else {

            List<RestService> servicesForO = new ArrayList<>();

            servicesForO.add(service);

            services.put(o, servicesForO);
        }
    }

    public static List<RestService> servicesFor(Object o) {

        if (o == null) {
            return Collections.emptyList();
        }

        List<RestService> servicesList = null;

        try {

            servicesList = services.get(o);

        } catch (Exception e) {
        }

        return servicesList != null ? servicesList : Collections.emptyList();
    }

}
