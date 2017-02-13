package mg.restgen.service;

import java.util.List;

import org.junit.Test;

public class ServiceCacheTest {

    /*
        - services do actions (interface RestAction.apply()): handle all business logic, have to be registered to function.
        - base RestService handles the Persistables: the find, findAll, findBy fields, filter by fields, free search etc.
        - TOIMPROVE: far away goal: handle JPAs and hibernates and all other type 'persistables' too
     */

    @Test
    public void test() {

        ServiceCache.register(TestValue.class, new TestService());

        @SuppressWarnings("unused")
        List<RestService> services = ServiceCache.servicesFor(TestValue.class);

    }

    public class TestService implements RestService {

        @Override
        public void apply(Object object) {

        }

        @Override
        public List<Class<?>> getAcceptableTypes() {
            return null;
        }

        @Override
        public List<String> getActions() {
            return null;
        }
    }

    public class TestValue {
    }

}
