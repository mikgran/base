package mg.restgen.service;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

public class ServiceCacheTest {

    /*
        - services do actions (interface RestAction.apply()): handle all business logic, have to be registered to function.
        - base RestService handles the Persistables: the find, findAll, findBy fields, filter by fields, free search etc.
        - TOIMPROVE: far away goal: handle JPAs and hibernates and all other type 'persistables' too
     */

    @Test
    public void testRegister() {

        Class<?> candidateClass = TestValue.class;

        ServiceCache.register(candidateClass, new TestService());

        List<RestService> services = ServiceCache.of(candidateClass);

        assertNotNull(services);
    }

    public class TestService implements RestService {

        @Override
        public void apply(Object object, Action action) {
            // TODO Auto-generated method stub
        }

        @Override
        public List<Class<?>> getAcceptableTypes() {
            return null;
        }

        @Override
        public List<Action> getActions() {
            return null;
        }
    }

    public class TestValue {
    }

}
