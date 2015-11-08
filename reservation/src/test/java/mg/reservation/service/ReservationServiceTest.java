package mg.reservation.service;

import static mg.util.Common.close;
import static mg.util.Common.yyyyMMddHHmmFormatter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import mg.reservation.dao.TestConfig;
import mg.reservation.dao.TestDBSetup;
import mg.reservation.db.DBConfig;
import mg.reservation.db.OverlappingReservationException;
import mg.reservation.db.Reservation;
import mg.reservation.db.ReservationDao;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReservationServiceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static ReservationServiceImpl reservationService = null;
	private static String dbName = "reservationtest2";
	private static DBConfig dbConfig = null;
	private static Connection connection = null;
	private final ReservationDao reservationDao = new ReservationDao();

	@BeforeClass
	public static void setupOnce() throws IOException {
		dbConfig = new DBConfig(new TestConfig(dbName));
		reservationService = new ReservationServiceImpl(dbConfig);
		connection = TestDBSetup.setupDbAndGetConnection(dbName);
	}

	@AfterClass
	public static void tearDownOnce() {
		close(connection);
	}

	@Test
	public void testCreatingReservation() throws IOException, ParseException, ClassNotFoundException, SQLException, IllegalArgumentException, OverlappingReservationException {

		String id = "AC1";
		String startTime = "2010-10-01 00:00";
		String endTime = "2010-10-01 03:00";
		String reserver = "Person";
		String resource = "Alpha";
		String title = "title";
		String description = "description";
		Reservation reservation = reservationFrom(id, resource, reserver, startTime, endTime, title, description);

		reservation = reservationService.createReservation(reservation);
		assertNotNull(reservation);
		assertEquals("there should be an id for the reservation", id, reservation.getId());

		Reservation storedReservationCandidate = reservationDao.findByPrimaryKey(connection, reservation.getId());
		assertNotNull(storedReservationCandidate);
		assertReservation(id, startTime, endTime, reserver, resource, title, description, storedReservationCandidate);

		// create the same reservation again
		thrown.expect(OverlappingReservationException.class);
		reservationService.createReservation(reservation);
	}

	@Test
	public void testDeletingReservation() throws ParseException, ClassNotFoundException, IllegalArgumentException, SQLException, OverlappingReservationException {

		String startTime = "2010-10-04 00:00";
		String endTime = "2010-10-04 02:00";
		String reserver = "Person";
		String resource = "Gamma";
		String title = "title";
		String description = "description";
		Reservation reservation = reservationFrom("AS2", resource, reserver, startTime, endTime, title, description);

		reservation = reservationService.createReservation(reservation);
		assertNotNull(reservation);

		reservation = reservationService.deleteReservation(reservation);
		assertNotNull(reservation);

		Reservation deletedReservationCandidate = reservationDao.findByPrimaryKey(connection, reservation.getId());
		assertNull(deletedReservationCandidate);
	}

	// TODO: expand coverage, more than just the happy path case.
	@Test
	public void testReschedulingReservation() throws ParseException, ClassNotFoundException, IllegalArgumentException, SQLException, OverlappingReservationException {

		String id = "A3";
		String id2 = "A4";
		String startTime = "2010-10-05 00:00";
		String endTime = "2010-10-05 02:00";
		String reserver = "Person";
		String resource = "Gamma";
		String title = "title";
		String description = "description";
		Reservation reservation = reservationFrom(id, resource, reserver, startTime, endTime, title, description);

		reservation = reservationService.createReservation(reservation);
		assertEquals("creating a reservation should create an id", id, reservation.getId());

		Reservation createdReservationCandidate = reservationDao.findByPrimaryKey(connection, reservation.getId());
		assertReservation(id, startTime, endTime, reserver, resource, title, description, createdReservationCandidate);

		String newStartTime = "2010-10-05 01:00";
		String newEndTime = "2010-10-05 03:00";

		Reservation newReservationCandidate = reservationFrom(id2, resource, reserver, newStartTime, newEndTime, title, description);
		Reservation oldReservationCandidate = reservationService.rescheduleReservation(createdReservationCandidate, newReservationCandidate);
		assertNotNull(oldReservationCandidate);

		Reservation rescheduledReservationCandidate = reservationDao.findByPrimaryKey(connection, newReservationCandidate.getId());
		assertReservation(id2, newStartTime, newEndTime, reserver, resource, title, description, rescheduledReservationCandidate);
	}

	private void assertReservation(String id, String startTime, String endTime, String reserver, String resource, String title, String description, Reservation reservationCandidate) {
		assertNotNull(reservationCandidate);
		assertEquals("id should be", id, reservationCandidate.getId());
		assertEquals("reserver should be", reserver, reservationCandidate.getReserver());
		assertEquals("resource should be", resource, reservationCandidate.getResource());
		assertEquals("title should be", title, reservationCandidate.getTitle());
		assertEquals("description should be", description, reservationCandidate.getDescription());
		assertEquals("start time should be", startTime, yyyyMMddHHmmFormatter.format(reservationCandidate.getStart()));
		assertEquals("end time should be", endTime, yyyyMMddHHmmFormatter.format(reservationCandidate.getEnd()));
	}

	private Reservation reservationFrom(String id, String resource, String reserver, String startTimeString, String endTimeString, String title, String description) throws ParseException {
		return new Reservation(id, resource, reserver, dateFrom(startTimeString), dateFrom(endTimeString), title, description);
	}

	private Date dateFrom(String dateString) throws ParseException {
		return yyyyMMddHHmmFormatter.parse(dateString);
	}

}
