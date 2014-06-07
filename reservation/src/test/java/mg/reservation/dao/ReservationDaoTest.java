package mg.reservation.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import mg.reservation.common.TestUtil;
import mg.reservation.db.Reservation;
import mg.reservation.db.ReservationDao;
import mg.reservation.util.Common;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReservationDaoTest extends TestUtil {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static Connection connection = null;
	private final ReservationDao reservationDao = new ReservationDao();

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
	public static void setupOnce() throws IOException {
		connection = TestDBSetup.setupDbAndGetConnection("reservationtest");
	}

	@AfterClass
	public static void tearDownOnce() throws SQLException {
		Common.close(connection);
	}

	@Test
	public void testFindingOverlappingDates() {
		try {

			// google for "Test If Date Ranges Overlap" for more information.
			String resource = "Beta";
			String reserver = "person";

			Reservation expectedReservation1 = reservationFrom("AA", resource, reserver, "2010-01-01 00:00", "2010-01-10 00:00", "title1", "desc1");
			Reservation expectedReservation2 = reservationFrom("BB", resource, reserver, "2010-01-20 00:59", "2010-01-30 00:00", "title2", "desc2");
			Reservation expectedReservation3 = reservationFrom("CC", resource, reserver, "2010-02-01 00:00", "2010-02-15 00:00", "title3", "desc3");

			List<Reservation> overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-21 00:00"), dateFrom("2010-01-29 00:00"));

			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have 1 overlapping reservation", 1, overlappingReservations.size());
			assertTrue("the list should contain reservation 2", listContains(overlappingReservations, expectedReservation2));

			overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-19 00:00"), dateFrom("2010-02-29 00:00"));

			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have 2 overlapping reservations", 2, overlappingReservations.size());
			assertTrue("the list should contain reservations 2 and 3", listContains(overlappingReservations, expectedReservation2, expectedReservation3));

			overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-02 00:00"), dateFrom("2010-01-25 00:00"));

			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have 2 overlapping reservations", 2, overlappingReservations.size());
			assertTrue("the list should contain reservations 1 and 2", listContains(overlappingReservations, expectedReservation1, expectedReservation2));

			overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-19 20:00"), dateFrom("2010-01-19 22:00"));
			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have no overlapping reservations", 0, overlappingReservations.size());

			overlappingReservations = reservationDao.findOverlappingByDates(connection, resource, dateFrom("2010-01-19 20:00"), dateFrom("2010-01-20 01:00"));
			assertNotNull("overlappingReservations should be not null.", overlappingReservations);
			assertEquals("the list should have no overlapping reservations", 1, overlappingReservations.size());

		} catch (Exception e) {
			failWithMessage(e);
		}
	}

	@Test
	public void testCreatingAndFindingReservation() throws ParseException {

		String resource = "Beta";
		String reserver = "person";
		Date startTime = dateFrom("2011-01-01 00:00");
		Date endTime = dateFrom("2011-01-01 01:00");
		String title = "reservation";
		String description = "desc";

		try {
			Reservation createdReservation = reservationDao.createReservation(connection, new Reservation("A", resource, reserver, startTime, endTime, title, description));

			assertNotNull(createdReservation);
			assertTrue("reservation should have an id", createdReservation.getId() != "");

			Reservation foundReservation = reservationDao.findByPrimaryKey(connection, createdReservation.getId());

			assertNotNull(foundReservation);
			assertEquals(resource, foundReservation.getResource());
			assertEquals(reserver, foundReservation.getReserver());
			assertEquals(startTime.getTime(), foundReservation.getStart().getTime());
			assertEquals(endTime.getTime(), foundReservation.getEnd().getTime());
			assertEquals(description, foundReservation.getDescription());

		} catch (Exception e) {
			failWithMessage(e);
		}
	}

	@Test
	public void testDeletingReservation() {

		try {
			Reservation storedReservation = reservationDao.createReservation(connection, new Reservation("A5", "Beta", "person", dateFrom("2011-01-01 00:00"), dateFrom("2011-01-01 01:00"), "title", "storing test"));
			int numberOfRowsAffected = reservationDao.deleteReservation(connection, new Reservation(storedReservation.getId(), "Beta", "person", dateFrom("2011-01-01 00:00"), dateFrom("2011-01-01 01:00"), "title", "storing test"));

			assertNotNull(storedReservation);
			assertTrue("reservation should have an id", storedReservation.getId() == "A5");

			assertEquals("deleting should return 1", 1, numberOfRowsAffected);

		} catch (Exception e) {
			failWithMessage(e);
		}

	}

	@Test
	public void testSelectingRangeOfReservations() {

		try {
			String resource = "Beta";
			String reserver = "person";
			String startTime = "2010-01-01 00:00";
			String endTime = "2010-01-10 00:00";
			String startTime2 = "2010-01-20 00:59";
			String endTime2 = "2010-01-30 00:00";

			Reservation expectedReservation1 = reservationFrom("AA", resource, reserver, startTime, endTime, "title1", "desc1");
			Reservation expectedReservation2 = reservationFrom("BB", resource, reserver, startTime2, endTime2, "title2", "desc2");

			List<Reservation> reservations = reservationDao.findByDates(connection, dateFrom(startTime), dateFrom(startTime));

			assertNotNull(reservations);
			assertTrue("reservations should have a size of 1", reservations.size() == 1);
			assertTrue("found cadidates should contain expected reservation 1", listContains(reservations, expectedReservation1));

			reservations = reservationDao.findByDates(connection, dateFrom(startTime), dateFrom(startTime2));
			assertNotNull(reservations);
			assertTrue("reservations should have a size of 2", reservations.size() == 2);
			assertTrue("found cadidates should contain expected reservations 1 and 2", listContains(reservations, expectedReservation1, expectedReservation2));

		} catch (Exception e) {
			failWithMessage(e);
		}
	}

	private void failWithMessage(Exception e) {
		fail(String.format("Error in test: %s.\n%s", e.getMessage(), stackTraceToString(e.getStackTrace())));
	}

	private Object stackTraceToString(StackTraceElement[] stackTrace) {

		StringBuilder stackTraceBuilder = new StringBuilder();
		stackTraceBuilder.append("");

		if (stackTrace != null) {
			for (StackTraceElement stackTraceElement : stackTrace) {
				stackTraceBuilder.append(stackTraceElement.toString());
				stackTraceBuilder.append("\n");
			}
		}

		return stackTraceBuilder.toString();
	}

	private Reservation reservationFrom(String id, String resource, String reserver, String startTimeString, String endTimeString, String title, String description) throws ParseException {
		return new Reservation(id, resource, reserver, dateFrom(startTimeString), dateFrom(endTimeString), title, description);
	}

	private Date dateFrom(String dateString) throws ParseException {
		return Common.yyyyMMddHHmmFormatter.parse(dateString);
	}

}
