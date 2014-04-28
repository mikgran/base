package mg.reservation.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ReservationService {

	// TODO replace these with configuration file fetch:
	private static final String USER_NAME = "testuser";
	private static final String PASSWORD = "testpass";
	private static final String DB_URL = "jdbc:mysql://localhost/reservation";
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

	private Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(DB_DRIVER);
		Connection connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
		return connection;
	}

	// TODO:

}
