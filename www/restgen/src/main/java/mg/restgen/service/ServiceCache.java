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

    private static ConcurrentHashMap<String, ServiceParameters> services = new ConcurrentHashMap<>();

    public static void register(Class<? extends Object> classRef, RestService service) {
        validateNotNull("service", service);
        validateNotNull("classRef", classRef);

        String key = classRef.getName();

        if (services.containsKey(key)) {

            ServiceParameters parameters = services.get(key);

            parameters.services.add(service);

        } else {
            List<RestService> restServices = new ArrayList<>();
            restServices.add(service);

            services.put(key, new ServiceParameters(restServices, classRef, key));
        }
    }

    public static ServiceParameters servicesFor(Class<? extends Object> o) {

        validateNotNull("o", o);

        ServiceParameters serviceParameters = null;

        try {

            serviceParameters = services.get(o.getName());

        } catch (Exception e) {
        }

        return serviceParameters != null ? serviceParameters : new ServiceParameters(Collections.emptyList(), null, null);
    }

    // TOIMPROVE: add Annotation scanner feature for @Service(AcceptableType="") (or include acceptable types in the
    // (OR: use scanner for for finding RestService interfaces)
    // service.getAcceptableTypes()) scanning that auto-registers
    // Services.scan("mg.package.name");

}
