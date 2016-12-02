package mg.util.db;

import static mg.util.validation.Validator.validateNotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import mg.util.Config;
import mg.util.validation.Validator;

public class DBConfig {

    public static final String NOT_DEFINED_IN_PROPERTIES = " not defined in properties.";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String DB_URL = "dbUrl";
    public static final String DB_DRIVER = "dbDriver";

    private static Properties properties = new Properties();
    private static DataSource dataSource = null;
    private static String dbDriver;
    private static String dbUrl;
    private static String userName;
    private static String password;

    private static DataSource getDataSourceInstance() throws SQLException {

        if (dataSource == null) {

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(dbDriver);
            basicDataSource.setUrl(dbUrl);
            basicDataSource.setUsername(userName);
            basicDataSource.setPassword(password);

            dataSource = basicDataSource;
            return dataSource;

        } else {
            return dataSource;
        }
    }

    /**
     * Creates a DBConfig using the supplied config.
     * @param config A configuration to use with accessing the database.
     * @throws IOException If unable to access the properties file.
     */
    public DBConfig(Config config) throws IOException {
        validateNotNull("config", config);
        properties = config.loadProperties();

        dbDriver = validateNotNull(DB_DRIVER + NOT_DEFINED_IN_PROPERTIES, properties.getProperty(DB_DRIVER));
        dbUrl = validateNotNull(DB_URL + NOT_DEFINED_IN_PROPERTIES, properties.getProperty(DB_URL));
        userName = validateNotNull(USER_NAME + NOT_DEFINED_IN_PROPERTIES, properties.getProperty(USER_NAME));
        password = validateNotNull(PASSWORD + NOT_DEFINED_IN_PROPERTIES, properties.getProperty(PASSWORD));
    }

    /**
     * Creates a new connection by using a singleton data source to get the connection.
     * Uses db driver name, db url, db username and db password for the access.
     *
     * @return The created database connection.
     * @throws ClassNotFoundException If the driver classes can not be loaded.
     * @throws SQLException If any database error occurs.
     */
    public Connection getConnection() throws ClassNotFoundException, SQLException {

        return getDataSourceInstance().getConnection();
    }
}
