package mg.restgen.service;

import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class CrudServiceTest {

    @Test
    public void testServicePut() {

        CrudService crudService = new CrudService();
        assertNotNull(crudService);

        Set<String> parameters = new HashSet<>();
        parameters.add("put");

        // FIXME: last -> ServiceCacheMock.register(class or object, service)
        crudService.apply(target, parameters);
    }

}
