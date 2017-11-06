package mg.restgen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import mg.restgen.service.ServiceCacheTest.TestKey2;

public class TestServiceKey {

    /**
     *
     */
    @Test
    public void testEquals() {

        String simpleName = TestKey2.class.getSimpleName();
        String command = "put";

        ServiceKey serviceKey = ServiceKey.of(simpleName, command);
        ServiceKey serviceKey2 = ServiceKey.of(simpleName, command);

        assertNotNull(serviceKey);
        assertNotNull(serviceKey2);
        assertEquals(serviceKey, serviceKey2);
    }
}
