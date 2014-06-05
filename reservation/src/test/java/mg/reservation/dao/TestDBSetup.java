package mg.reservation.dao;

import static org.junit.Assert.fail;
import static mg.reservation.util.Common.close;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mg.reservation.db.DBConfig;

public class TestDBSetup {

	private static final String RESERVATIONS_TEST_DB_DROP = "DROP TABLE IF EXISTS reservations;";

	// resources and reservers could be references to other tables, but mvp: keeping it simple here (reservation.resource 0..n___1 resource.id)
	private static final String RESERVATIONS_TEST_DB_CREATE = "CREATE TABLE reservations (" +
			"id VARCHAR(100) NOT NULL," +
			"resource VARCHAR(40) NOT NULL," +
			"reserver VARCHAR(60) NOT NULL," +
			"start_time DATETIME NOT NULL," +
			"end_time DATETIME NOT NULL," +
			"title VARCHAR(100) NOT NULL," +
			"description VARCHAR(500)," +
			"PRIMARY KEY(ID));";

	private static final String RESERVATIONS_TEST_DATA_INSERT = "INSERT INTO reservations" +
			"(id, resource, reserver, start_time, end_time, title, description) VALUES" +
			"('AA', 'Beta', 'person', '2010-01-01 00:00', '2010-01-10 00:00', 'title1', 'desc1')," +
			"('BB', 'Beta', 'person', '2010-01-20 00:59', '2010-01-30 00:00', 'title2', 'desc2')," +
			"('CC', 'Beta', 'person', '2010-02-01 00:00', '2010-02-15 00:00', 'title3', 'desc3')," +
			"('DD', 'Beta', 'person', '2014-05-26 12:00', '2014-05-26 13:00', 'title4', 'desc4');";

	public static Connection setupDbAndGetConnection(String dbName) throws IOException {
		Statement s1 = null;
		Statement s2 = null;
		Statement s3 = null;
		Connection connection = null;

		try {
			DBConfig dbConfig = new DBConfig(new TestConfig(dbName));
			connection = dbConfig.getConnection();

			s1 = connection.createStatement();
			s1.executeUpdate(RESERVATIONS_TEST_DB_DROP);

			s2 = connection.createStatement();
			s2.executeUpdate(RESERVATIONS_TEST_DB_CREATE);

			s3 = connection.createStatement();
			s3.executeUpdate(RESERVATIONS_TEST_DATA_INSERT);

		} catch (SQLException e) {
			fail("Error initializing the database: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			fail("Error initializing the database: " + e.getMessage());
		} finally {
			close(s1, s2, s3);
		}
		return connection;
	}

}
