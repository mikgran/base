package mg.util.db.persist.proxy;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ListProxyTest {

    private static final String TEST_VALUE = "testValue";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testProxyChain() throws Exception {

        List<String> list = new ArrayList<>();
        list.add(TEST_VALUE);

        list = ListProxy.newInstance(list);

        assertEquals("proxy list should have the size of:", 1, list.size());
        assertEquals("proxy list should contain: ", TEST_VALUE, list.get(0));

        list.add(TEST_VALUE + "2");

        assertEquals("proxy list should have the size of: ", 2, list.size());
        assertEquals("proxy list should contain: ", TEST_VALUE + "2", list.get(1));

        Optional<String> s = list.stream()
                                 .collect(Collectors.reducing((a, b) -> a + b));

        assertEquals("", TEST_VALUE + TEST_VALUE + "2", s.get());

    }

}
