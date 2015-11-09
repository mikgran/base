package mg.util.db;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mg.util.TestConfig;
import mg.util.db.DBConfig;

public class TestDBSetup {

    /**
     * Setups a connection for dbName string and performs any number of initialisation
     * strings on the given database.
     * 
     * @param dbName
     * @param initializationSqlStrings
     * @return
     * @throws IOException
     */
    public static Connection setupDbAndGetConnection(String dbName, String... initializationSqlStrings) throws IOException {

        Connection connection = null;

        try {

            DBConfig dbConfig = new DBConfig(new TestConfig(dbName));
            connection = dbConfig.getConnection();

            if (initializationSqlStrings != null &&
                initializationSqlStrings.length > 0) {

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
