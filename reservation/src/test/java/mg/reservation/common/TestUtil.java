package mg.reservation.common;

import java.util.List;

import mg.reservation.db.Reservation;

public class TestUtil {

	protected boolean listContains(List<Reservation> reservations, Reservation... expectedReservations) {
	
		int expectedNumberOfReservations = expectedReservations.length;
		int foundReservations = 0;
	
		for (Reservation reservation : reservations) {
	
			for (Reservation expectedReservation : expectedReservations) {
	
				if (expectedReservation.getId().equals(reservation.getId()) &&
						expectedReservation.getStartTime().getTime() == reservation.getStartTime().getTime() &&
						expectedReservation.getEndTime().getTime() == reservation.getEndTime().getTime()) {
	
					foundReservations += 1;
				}
			}
		}
	
		return (expectedNumberOfReservations == foundReservations);
	}

}
