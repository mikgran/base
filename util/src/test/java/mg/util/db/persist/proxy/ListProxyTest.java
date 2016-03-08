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
import mg.util.db.persist.SqlLazyBuilder;
import mg.util.db.persist.support.Person3;
import mg.util.db.persist.support.Todo3;

public class ListProxyTest {

    private static Connection connection;
    private static final String TEST_VALUE = "testValue";
    private static Todo3 todo3;

    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");

        DB db = new DB(connection);

        Person3 person3 = new Person3("testLP1", "valueLP2");
        db.createTable(person3);
        db.save(person3);

        todo3 = new Todo3(TEST_VALUE, person3.getId());
        db.createTable(todo3);
        db.save(todo3);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // @Ignore
    @Test
    public void testProxyChain() throws Exception {

        DB db = new DB(connection);

        ArrayList<Todo3> todoList = new ArrayList<Todo3>();
        todoList.add(todo3);
        SqlLazyBuilder sqlLazyBuilder = new SqlLazyBuilder(todo3);
        String buildSelectByIds = sqlLazyBuilder.buildSelectByIds();

        DBProxyParameters<List<Todo3>> listProxyParameters = new DBProxyParameters<List<Todo3>>(db, todoList, buildSelectByIds, todo3);

        List<Todo3> proxyList = DBProxy.newList(listProxyParameters);

        assertEquals("proxy list should have the size of:", 1,
                     proxyList.size());
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
