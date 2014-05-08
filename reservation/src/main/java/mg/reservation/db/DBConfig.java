package mg.reservation.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import mg.reservation.util.Config;

import org.apache.commons.dbcp.BasicDataSource;

// TODO add connection pooling
public class DBConfig {

	// TODO replace these with configuration file fetch:
	private static final String USER_NAME = "userName";
	private static final String PASSWORD = "password";
	private static final String DB_URL = "dbUrl";
	private static final String DB_DRIVER = "dbDriver";

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
	 * Creates a new pooled connection by loading the database driver and using the 
	 * drivermanager to get a connection.
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

		Class.forName(dbDriver);

		BasicDataSource poolingDataSource = new BasicDataSource();
		poolingDataSource.setDriverClassName(dbDriver);
		poolingDataSource.setUrl(dbUrl);
		poolingDataSource.setUsername(userName);
		poolingDataSource.setPassword(password);

		return poolingDataSource.getConnection();
	}

}
