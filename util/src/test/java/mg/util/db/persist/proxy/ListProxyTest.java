package mg.util.db.persist.proxy;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.db.persist.support.Person3;
import mg.util.db.persist.support.Todo3;
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

    private ThrowingConsumer<ListProxyParameters<List<Person3>>, Exception> stringListProcessor = (listProxyParameters) -> {

    };

    @Test
    public void testProxyChain() throws Exception {

        DB db = new DB(connection);

        // replace nulls with list proxies.

        ListProxyParameters<List<Todo3>> listProxParams = new ListProxyParameters<List<Todo3>>(db, new ArrayList<Todo3>());



        new Person3(TEST_VALUE, "", null);

//
//        list = ListProxy.newInstance(listProxParams, stringListProcessor);
//
//        assertEquals("proxy list should have the size of:", 1, list.size());
//        assertEquals("proxy list should contain: ", TEST_VALUE, list.get(0).getFirstName());
//
//        list.add(TEST_VALUE + "2");
//
//        assertEquals("proxy list should have the size of: ", 2, list.size());
//        assertEquals("proxy list should contain: ", TEST_VALUE + "2", list.get(1));
//
//        String s = list.stream()
//                       .reduce("", (a, b) -> a + b);
//
//        assertEquals("after reduction of the list test, the string should be: ", TEST_VALUE + TEST_VALUE + "2", s);
//
    }

}
