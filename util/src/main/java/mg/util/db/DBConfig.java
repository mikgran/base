package mg.util.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;

import mg.util.Config;

public class DBConfig {

	public static final String USER_NAME = "userName";
	public static final String PASSWORD = "password";
	public static final String DB_URL = "dbUrl";
	public static final String DB_DRIVER = "dbDriver";

	private static Properties properties = new Properties();

	/**
	 * Creates a DBConfig using the supplied config.
	 * @param config A configuration to use with accessing the database.
	 * @throws IOException If unable to access the properties file.
	 */
	public DBConfig(Config config) throws IOException {
		properties = config.loadProperties();
	}

	/**
	 * Creates a new connection and using a pooled datasource to get a connection.
	 * Uses db driver name, db url, db username and db password for the access.
	 * 
	 * @return The created database connection.
	 * @throws ClassNotFoundException If the driver classes can not be loaded.
	 * @throws SQLException If any database error occurs.
	 */
	public Connection getConnection() throws ClassNotFoundException, SQLException {

		String dbDriver = properties.getProperty(DB_DRIVER);
		String dbUrl = properties.getProperty(DB_URL);
		String userName = properties.getProperty(USER_NAME);
		String password = properties.getProperty(PASSWORD);

		BasicDataSource poolingDataSource = new BasicDataSource();
		poolingDataSource.setDriverClassName(dbDriver);
		poolingDataSource.setUrl(dbUrl);
		poolingDataSource.setUsername(userName);
		poolingDataSource.setPassword(password);

		return poolingDataSource.getConnection();
	}

}
