package mg.reservation.db;

import static mg.reservation.validation.rule.ValidationRule.NOT_NULL;
import static mg.reservation.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import mg.reservation.util.Config;
import mg.reservation.validation.Validator;

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

		new Validator()
				.add("properties", properties, NOT_NULL)
				.validate();

		properties = config.loadProperties();
	}

	/**
	 * Creates a new connection by loading the database driver and using the 
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

		new Validator().add("dbDriver (property)", dbDriver, NOT_NULL_OR_EMPTY_STRING)
				.add("dbUrl (property)", dbUrl, NOT_NULL_OR_EMPTY_STRING)
				.add("userName (property)", userName, NOT_NULL_OR_EMPTY_STRING)
				.add("password (property)", password, NOT_NULL_OR_EMPTY_STRING)
				.validate();

		Class.forName(dbDriver);
		Connection connection = DriverManager.getConnection(dbUrl, userName, password);

		return connection;
	}
}
