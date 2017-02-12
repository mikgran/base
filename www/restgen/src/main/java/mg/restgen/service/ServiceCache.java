package mg.restgen.service;

import java.util.List;

import mg.restgen.service.ServiceCacheTest.RestService;
import mg.restgen.service.ServiceCacheTest.TestService;
import mg.restgen.service.ServiceCacheTest.TestValue;

// TOIMPROVE: generalise even further? 'ObjectCache'?
public class ServiceCache {

    public static void register(Class<TestValue> class1, TestService testService) {

    }

    public static List<RestService> servicesFor(Class<TestValue> class1) {
        return null;
    }

}
