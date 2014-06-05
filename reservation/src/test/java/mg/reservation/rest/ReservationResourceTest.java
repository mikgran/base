package mg.reservation.rest;

import static mg.reservation.util.Common.close;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import mg.reservation.common.TestUtil;
import mg.reservation.dao.TestConfig;
import mg.reservation.dao.TestDBSetup;
import mg.reservation.db.DBConfig;
import mg.reservation.db.Reservation;
import mg.reservation.service.ReservationService;
import mg.reservation.util.Common;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReservationResourceTest extends TestUtil {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static ReservationService reservationService = null;
	private static String dbName = "reservationtest2";
	private static DBConfig dbConfig = null;
	private static Connection connection = null;
	private static ReservationResource testResource = null;

	// 'DD', 'Beta', 'person', '2014-05-26 12:00', '2014-05-26 13:00', 'title4', 'desc4'
	private Reservation expectedReservation = new Reservation(
			"DD",
			"Beta",
			"person",
			Common.getDateFrom("1401094800000"),
			Common.getDateFrom("1401098400000"),
			"title4",
			"desc4"
			);

	@BeforeClass
	public static void setupOnce() throws IOException {
		dbConfig = new DBConfig(new TestConfig(dbName));
		reservationService = new ReservationService(dbConfig);
		connection = TestDBSetup.setupDbAndGetConnection(dbName);
		testResource = new ReservationResource(reservationService);
	}

	@AfterClass
	public static void tearDownOnce() {
		close(connection);
	}

	@Test
	public void testQueryReservations() throws Exception {

		List<Reservation> reservations = testResource.queryReservations("1401094800000", "1401098400000");
		assertNotNull(reservations);
		assertEquals("there should be reservations", 1, reservations.size());
		assertTrue(listContains(reservations, expectedReservation));
	}

	@Test
	public void testQueryNoContent() {
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("HTTP 204 No Content");
		testResource.queryReservations("1400094800000", "1400098400000");
	}

	@Test
	public void testQueryBadRequest() {
		thrown.expect(WebApplicationException.class);
		thrown.expectMessage("HTTP 400 Bad Request");
		testResource.queryReservations("0", "1");
	}

}