package mg.reservation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import mg.reservation.db.DBConfig;
import mg.reservation.db.OverlappingReservationException;
import mg.reservation.db.Reservation;
import mg.reservation.db.ReservationDao;
import mg.reservation.util.Config;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReservationServiceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static ReservationService reservationService = null;
	private static DBConfig dbConfig = null;

	@BeforeClass
	public static void setupOnce() throws IOException {
		dbConfig = new DBConfig(new Config());
		reservationService = new ReservationService(dbConfig);
	}

	@AfterClass
	public static void tearDownOnce() {
		
	}

	@Test
	public void testService() throws IOException, ParseException, ClassNotFoundException, SQLException, IllegalArgumentException, OverlappingReservationException {

		// TODO:
		// - add connection pooling to DBConfig
		// - service calls for adding a reservation, deleting an reservation, rescheduling an reservation

		String startTime = "2010-10-01 00:00";
		String endTime = "2010-10-01 03:00";
		String reserver = "Person";
		String resource = "Alpha";
		String title = "title";
		String description = "description";
		Reservation reservation = reservationFrom(0, resource, reserver, startTime, endTime, title, description);

		reservation = reservationService.createReservation(reservation);

		assertNotNull(reservation);
		assertTrue("there should be an id for the reservation", reservation.getId() > 0);

		ReservationDao reservationDao = new ReservationDao();
		Reservation storedReservationCandidate = reservationDao.findByPrimaryKey(dbConfig.getConnection(), reservation.getId());
		assertNotNull(storedReservationCandidate);
		assertEquals("reserver should be", reserver, storedReservationCandidate.getReserver());
		assertEquals("resource should be", resource, storedReservationCandidate.getResource());
		assertEquals("description should be", description, storedReservationCandidate.getDescription());

	}

	private Reservation reservationFrom(int id, String resource, String reserver, String startTimeString, String endTimeString, String title, String description) throws ParseException {
		return new Reservation(id, resource, reserver, dateFrom(startTimeString), dateFrom(endTimeString), title, description);
	}

	private Date dateFrom(String dateString) throws ParseException {
		return dateFormatter.parse(dateString);
	}

}
