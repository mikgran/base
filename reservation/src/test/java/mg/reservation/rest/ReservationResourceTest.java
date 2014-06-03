package mg.reservation.rest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mg.reservation.db.Reservation;
import mg.reservation.service.ReservationService;
import mg.reservation.util.Common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReservationResourceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private ArrayList<Reservation> reservations = new ArrayList<Reservation>();
	private ReservationResource testResource = new ReservationResource(newTestReservationService());
	private Reservation testReservation = new Reservation(
			"id",
			"resource",
			"reserver",
			Common.getDateFrom("1401094800000"),
			Common.getDateFrom("1401098400000"),
			"title",
			"description"
			);

	public ReservationResourceTest() {
		reservations.add(testReservation);
	}

	private ReservationService newTestReservationService() {
		return new ReservationService(null) {
			
			@Override
			public List<Reservation> findReservations(Date startTime, Date endTime) throws ClassNotFoundException, SQLException {
				return reservations;
			}
		};
	}

	@Test
	public void testQueryReservations() throws Exception {
		
		testResource.queryReservations("1401094800000", "1401098400000");

		// thrown.expect(IllegalArgumentException.class);
		// thrown.expectMessage(ARG_1 + " can not be null.");
	}

}
