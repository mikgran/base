package mg.restgen.service;

import org.junit.Test;

public class ServiceCacheTest {

    // 1. MemDB tests
    // 2. Spock?
    // 3. ServiceCache + tests

    @Test
    public void testRegistering() {

        ServiceCache.register(new ContactService());
    }

}
