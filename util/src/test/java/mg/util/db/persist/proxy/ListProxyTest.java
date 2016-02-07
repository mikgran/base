package mg.util.db.persist.proxy;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.functional.consumer.ThrowingConsumer;

public class ListProxyTest {

    private static Connection connection;
    private static final String TEST_VALUE = "testValue";

    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ThrowingConsumer<ListProxyParameters<List<String>>, Exception> stringListProcessor = (list) -> {
        // TODO
    };

    @Test
    public void testProxyChain() throws Exception {

        DB db = new DB(connection);

        List<String> list = new ArrayList<>();
        list.add(TEST_VALUE);

        ListProxyParameters<List<String>> listProxParams = new ListProxyParameters<List<String>>(db, list);

        list = ListProxy.newInstance(listProxParams, stringListProcessor);

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
