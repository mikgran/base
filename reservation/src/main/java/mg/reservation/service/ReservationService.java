package mg.reservation.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mg.reservation.db.OverlappingReservationException;
import mg.reservation.db.Reservation;

public interface ReservationService {

	Reservation createReservation(Reservation reservation) throws OverlappingReservationException, ClassNotFoundException, SQLException, IllegalArgumentException;

	Reservation deleteReservation(Reservation reservation) throws ClassNotFoundException, SQLException;

	Reservation rescheduleReservation(Reservation oldReservation, Reservation newReservation) throws OverlappingReservationException, ClassNotFoundException, SQLException;

	List<Reservation> findReservations(Date startTime, Date endTime) throws ClassNotFoundException, SQLException;

}
