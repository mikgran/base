package mg.util.db.persist.proxy;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.db.persist.support.Todo3;

public class ListProxyTest {

    private static Connection connection;
    private static final String TEST_VALUE = "testValue";

    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testProxyChain() throws Exception {

        DB db = new DB(connection);

        ArrayList<Todo3> todoList = new ArrayList<Todo3>();
        todoList.add(new Todo3(TEST_VALUE));
        ListProxyParameters<List<Todo3>> listProxyParameters = new ListProxyParameters<List<Todo3>>(db, todoList);

        List<Todo3> proxyList = ListProxy.newInstance(listProxyParameters);

        assertEquals("proxy list should have the size of:", 1, proxyList.size());
        assertEquals("proxy list get(0).getFirstName should be: ", TEST_VALUE, proxyList.get(0).getTodo());

        proxyList.add(new Todo3(TEST_VALUE + "2"));

        assertEquals("proxy list should have the size of: ", 2, proxyList.size());
        assertEquals("proxy list should contain: ", TEST_VALUE + "2", proxyList.get(1).getTodo());

        String s = proxyList.stream()
                            .map(todo -> todo.getTodo())
                            .reduce("", (a, b) -> a + b);

        assertEquals("after reduction of the list test, the string should be: ", TEST_VALUE + TEST_VALUE + "2", s);
    }

}
