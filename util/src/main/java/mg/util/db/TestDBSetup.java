package mg.util.db;

import static mg.util.Common.hasContent;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mg.util.TestConfig;

public class TestDBSetup {

    /**
     * Setups a connection for dbName string and performs any number of
     * initialisation strings on the given database.<br/><br/>
     *
     * If the setup fails for any reason fail() is called with the error message.
     *
     * @param dbName
     * @param initializationSqlStrings
     * @return the connection for the test database for the dbName
     * @throws IOException If no property file can be read.
     */
    public static Connection setupDbAndGetConnection(String dbName, String... initializationSqlStrings) throws IOException {

        Connection connection = null;

        try {

            DBConfig dbConfig = new DBConfig(new TestConfig());
            connection = dbConfig.getConnection();

            if (hasContent(initializationSqlStrings)) {

                for (String initializationSqlString : initializationSqlStrings) {

                    try (Statement statement = connection.createStatement()) {

                        statement.executeUpdate(initializationSqlString);
                    }
                }
            }

        } catch (SQLException | ClassNotFoundException e) {

            fail("Error initializing the database: " +
                e.getMessage());
        }

        return connection;
    }

}
