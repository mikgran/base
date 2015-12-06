package mg.util.validation.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConnectionNotClosedRuleTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConnectionAnotherObject() throws SQLException {

        ConnectionNotClosed connectionOpenRule = new ConnectionNotClosed();

        assertFalse("a non Connection connection should return false.", connectionOpenRule.apply(new String("")));
    }

    @Test
    public void testConnectionClosed() throws SQLException {

        Connection connection = getMockedConnection(true);

        ConnectionNotClosed connectionOpenRule = new ConnectionNotClosed();

        assertFalse("a closed connection should return false.", connectionOpenRule.apply(connection));
    }

    @Test
    public void testConnectionNull() throws SQLException {

        ConnectionNotClosed connectionOpenRule = new ConnectionNotClosed();

        assertFalse("a null connection should return false.", connectionOpenRule.apply(null));
    }

    @Test
    public void testConnectionOpen() throws SQLException {

        Connection connection = getMockedConnection(false);

        ConnectionNotClosed connectionOpenRule = new ConnectionNotClosed();

        assertTrue("an open connection should return true.", connectionOpenRule.apply(connection));
    }

    private Connection getMockedConnection(final boolean isOpenReturnValue) throws SQLException {
        Connection connection = context.mock(Connection.class);

        context.checking(new Expectations() {
            {
                oneOf(connection).isClosed();
                will(returnValue(isOpenReturnValue));

            }
        });
        return connection;
    }

}
