package mg.restgen.service;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;

public class CrudServiceTest {

    @Test
    public void testServicePut() {

        CrudService crudService = new CrudService();
        assertNotNull(crudService);

    }

}
