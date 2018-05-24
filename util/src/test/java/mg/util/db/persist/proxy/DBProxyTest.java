package mg.util.db.persist.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.DB;
import mg.util.db.persist.SqlLazyBuilder;
import mg.util.db.persist.support.Person6;
import mg.util.db.persist.support.Todo6;

public class DBProxyTest {

    private static Connection connection;
    private static final String NEW_FIRST_NAME1 = "testLP1";
    private static Person6 Person6;
    private static final String TEST_VALUE = "testValue";
    private static Todo6 todo6;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupOnce() throws Exception {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");

        DB db = new DB(connection);

        // TOIMPROVE: change to use unique test classes, so that cleanup is possible
        Person6 = new Person6("testLP1", "valueLP2");
        db.createTable(Person6);
        db.save(Person6);

        todo6 = new Todo6(TEST_VALUE, Person6.getId());
        db.createTable(todo6);
        db.save(todo6);
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        Common.close(connection);
    }

    @Ignore
    @Test
    public void testProxyChain() throws Exception {

        DB db = new DB(connection);

        ArrayList<Todo6> todoList = new ArrayList<>();
        todoList.add(todo6);
        SqlLazyBuilder sqlLazyBuilder = new SqlLazyBuilder(todo6);
        String buildSelectByIds = sqlLazyBuilder.buildSelectByIds();

        DBProxyParameters<List<Todo6>> listProxyParameters = new DBProxyParameters<>(db, todoList, buildSelectByIds, todo6, true);

        List<Todo6> proxyList = DBListProxy.newList(listProxyParameters);

        assertEquals("proxy list should have the size of:", 1,
                     proxyList.size());
        assertEquals("proxy list get(0).getFirstName should be: ", TEST_VALUE, proxyList.get(0).getTodo());

        proxyList.add(new Todo6(TEST_VALUE + "2"));

        assertEquals("proxy list should have the size of: ", 2, proxyList.size());
        assertEquals("proxy list should contain: ", TEST_VALUE + "2", proxyList.get(1).getTodo());

        String s = proxyList.stream()
                            .map(todo -> todo.getTodo())
                            .reduce("", (a, b) -> a + b);

        assertEquals("after reduction of the list test, the string should be: ", TEST_VALUE + TEST_VALUE + "2", s);

        SqlLazyBuilder sqlLazyBuilder2 = new SqlLazyBuilder(Person6);
        String buildSelectByIds2 = sqlLazyBuilder2.buildSelectByIds();
        DBProxyParameters<Person6> dbProxyParameters = new DBProxyParameters<>(db, Person6, buildSelectByIds2, Person6, true);

        Person6 proxyPerson = DBProxy.newInstance(dbProxyParameters);

        assertNotNull(proxyPerson);
        proxyPerson.setFirstName(NEW_FIRST_NAME1);
        assertEquals("original object should have firstName: ", NEW_FIRST_NAME1, Person6.getFirstName());

    }

}
