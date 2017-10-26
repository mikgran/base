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

    private static ConcurrentHashMap<String, List<RestService>> services = new ConcurrentHashMap<>();

    public static void register(Class<? extends Object> o, RestService service) {
        validateNotNull("service", service);
        validateNotNull("o", o);

        String key = o.getName();

        if (services.containsKey(key)) {

            List<RestService> servicesForO = services.get(key);

            servicesForO.add(service);

        } else {

            List<RestService> servicesForO = new ArrayList<>();

            servicesForO.add(service);

            services.put(key, servicesForO);
        }
    }

    public static List<RestService> servicesFor(Class<? extends Object> o) {

        validateNotNull("o", o);

        List<RestService> servicesList = null;

        try {

            servicesList = services.get(o.getName());

        } catch (Exception e) {
        }

        return servicesList != null ? servicesList : Collections.emptyList();
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
