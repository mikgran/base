package mg.restgen.service;

import org.junit.Test;

public class ServiceCacheTest {

    @Test
    public void testRegistering() {

        ServiceCache.register(new ContactService());
    }

}
