package mg.reservation.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import mg.reservation.util.Common;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReservationDaoTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static Connection connection = null;
	private static final String USER_NAME = "testuser";
	private static final String PASSWORD = "testpass";
	private static final String DB_URL = "jdbc:mysql://localhost/reservationtest";
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private final ReservationDao reservationDao = new ReservationDao();

	private static final String RESERVATIONS_TEST_DB_DROP = "DROP TABLE IF EXISTS reservations;";

	// resources and reservers could be references to other tables, but mvp: keeping it simple here (reservation.resource 0..n___1 resource.id)
	private static final String RESERVATIONS_TEST_DB_CREATE = "CREATE TABLE reservations (" +
			"id INT NOT NULL AUTO_INCREMENT," +
			"resource VARCHAR(40) NOT NULL," +
			"reserver VARCHAR(60) NOT NULL," +
			"start_time DATETIME NOT NULL," +
			"end_time DATETIME NOT NULL," +
			"description VARCHAR(500)," +
			"PRIMARY KEY(ID));";

	private static final String RESERVATIONS_TEST_DATA_INSERT = "INSERT INTO reservations" +
			"(resource, reserver, start_time, end_time, description) VALUES" +
			"('Beta', 'person', '2010-01-01', '2010-01-10', 'first')," +
			"('Beta', 'person', '2010-01-20', '2010-01-30', 'second')," +
			"('Beta', 'person', '2010-02-01', '2010-02-15', 'third');";

	// +----+---------------------+---------------------+-------------+
	// | id | start_time | end_time | description |
	// +----+---------------------+---------------------+-------------+
	// | 1 | 2010-01-01 00:00:00 | 2010-01-10 00:00:00 | first |
	// | 2 | 2010-01-20 00:00:00 | 2010-01-30 00:00:00 | second |
	// | 3 | 2010-02-01 00:00:00 | 2010-02-15 00:00:00 | third |
	// +----+---------------------+---------------------+-------------+
	// SELECT * FROM reservation.reservations WHERE "2010-01-21" < end_time AND "2010-01-29" > start_time; // old overlaps the new
	// SELECT * FROM reservation.reservations WHERE "2010-01-19" < end_time AND "2010-01-31" > start_time; // new overlaps the old
	// SELECT * FROM reservation.reservations WHERE "2010-01-19" < end_time AND "2010-01-21" > start_time; // new ends inside old
	// SELECT * FROM reservation.reservations WHERE "2010-01-21" < end_time AND "2010-01-31" > start_time; // new starts inside old

	// TOIMPROVE: replace with memory db instead? and use DBUnit perhaps?
	@BeforeClass
	public static void setupOnce() {
		Statement s1 = null;
		Statement s2 = null;
		Statement s3 = null;
		try {
			Class.forName(DB_DRIVER);
			connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);

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
			Common.close(s1);
			Common.close(s2);
			Common.close(s3);
		}
	}

	@AfterClass
	public static void tearDownOnce() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindingOverlappingDates() {
		try {

			// google for "Test If Date Ranges Overlap" for more information.
			String resource = "Beta";
			String reserver = "person";
			Reservation expectedReservation1 = reservationFrom(1, resource, reserver, "2010-01-01 00:00", "2010-01-10 00:00", "");
			Reservation expectedReservation2 = reservationFrom(2, resource, reserver, "2010-01-20 00:00", "2010-01-30 00:00", "");
			Reservation expectedReservation3 = reservationFrom(3, resource, reserver, "2010-02-01 00:00", "2010-02-15 00:00", "");

			List<Reservation> overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-21 00:00"), dateFrom("2010-01-29 00:00"));

			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have 1 overlapping reservation", 1, overlappingReservations.size());
			assertTrue("the list should contain reservation 2", listContains(expectedReservation2, overlappingReservations));

			overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-19 00:00"), dateFrom("2010-02-29 00:00"));

			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have 2 overlapping reservations", 2, overlappingReservations.size());
			assertTrue("the list should contain reservation 2", listContains(expectedReservation2, overlappingReservations));
			assertTrue("the list should contain reservation 3", listContains(expectedReservation3, overlappingReservations));

			overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-02 00:00"), dateFrom("2010-01-25 00:00"));

			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have 2 overlapping reservations", 2, overlappingReservations.size());
			assertTrue("the list should contain reservation 1", listContains(expectedReservation1, overlappingReservations));
			assertTrue("the list should contain reservation 2", listContains(expectedReservation2, overlappingReservations));

			overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-19 20:00"), dateFrom("2010-01-19 22:00"));
			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have no overlapping reservations", 0, overlappingReservations.size());

		} catch (Exception e) {
			failWithMessage(e);
		}
	}

	@Test
	public void testStoringReservation() {

		try {
			Reservation storedReservation = reservationDao.storeReservation(connection, new Reservation(0, "Beta", "person", dateFrom("2011-01-01 00:00"), dateFrom("2011-01-01 01:00"), ""));
			assertNotNull(storedReservation);
			assertTrue("reservation should have an id", storedReservation.getId() > -1);
		} catch (Exception e) {
			failWithMessage(e);
		}
	}

	@Test
	public void testDeletingReservation() {

		try {
			Reservation storedReservation = reservationDao.storeReservation(connection, new Reservation(0, "Beta", "person", dateFrom("2011-01-01 00:00"), dateFrom("2011-01-01 01:00"), "storing test"));
			int numberOfRowsAffected = reservationDao.deleteReservation(connection, new Reservation(storedReservation.getId(), "Beta", "person", dateFrom("2011-01-01 00:00"), dateFrom("2011-01-01 01:00"), "storing test"));

			assertNotNull(storedReservation);
			assertTrue("reservation should have an id", storedReservation.getId() > -1);

			assertEquals("deleting should return 1", 1, numberOfRowsAffected);

		} catch (Exception e) {
			failWithMessage(e);
		}

	}

	private void failWithMessage(Exception e) {
		// TODO: mvp, replace this with logging instead
		e.printStackTrace();
		fail("Error in test: " + e.getMessage());
	}

	private boolean listContains(Reservation expectedReservation, List<Reservation> listOfReservations) {

		for (Reservation reservation : listOfReservations) {
			if (expectedReservation.getId() == reservation.getId() &&
					expectedReservation.getStartTime().getTime() == reservation.getStartTime().getTime() &&
					expectedReservation.getEndTime().getTime() == reservation.getEndTime().getTime()) {

				return true;
			}
		}

		return false;
	}

	private Reservation reservationFrom(int id, String resource, String reserver, String startTimeString, String endTimeString, String description) throws ParseException {
		return new Reservation(id, resource, reserver, dateFrom(startTimeString), dateFrom(endTimeString), description);
	}

	private Date dateFrom(String dateString) throws ParseException {
		return dateFormatter.parse(dateString);
	}

}
