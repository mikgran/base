package mg.restgen.service;

import java.util.List;

import org.junit.Test;

public class ServiceCacheTest {

    @Test
    public void test() {

        ServiceCache.register(TestValue.class, new TestService());

        List<RestService> services = ServiceCache.servicesFor(TestValue.class);

    }

    public class TestService extends ContactService {

    }

    public class TestValue {

    }

}
