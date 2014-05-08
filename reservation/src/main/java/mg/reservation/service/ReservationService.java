package mg.reservation.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import mg.reservation.db.DBConfig;
import mg.reservation.db.OverlappingReservationException;
import mg.reservation.db.Reservation;
import mg.reservation.db.ReservationDao;
import mg.reservation.util.Common;

public class ReservationService {

	private DBConfig dbConfig = null;
	private ReservationDao reservationDao = new ReservationDao();

	public ReservationService(DBConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	/**
	 * Persists a reservation into the database. 
	 * 
	 * @param reservation 
	 * 	The reservation to persist.
	 * @return The the persisted reservation filled with the autogenerated id.
	 * @throws ClassNotFoundException If the database driver can not be loaded.
	 * @throws SQLException On any database errors.
	 * @throws IllegalArgumentException If any of the properties are null.
	 * @throws OverlappingReservationException If the reservation to be created overlaps with any existing reservation. 
	 */
	public Reservation createReservation(Reservation reservation) throws
			ClassNotFoundException,
			SQLException,
			IllegalArgumentException,
			OverlappingReservationException {

		Connection connection = null;
		Reservation reservationStored = null;

		try {
			connection = dbConfig.getConnection();

			List<Reservation> overlappingReservations = reservationDao.findOverlappingByDates(connection, reservation.getResource(), reservation.getStartTime(), reservation.getEndTime());
			if (overlappingReservations.size() > 0) {
				throw new OverlappingReservationException();
			}

			reservationStored = reservationDao.createReservation(connection, reservation);

		} finally {
			Common.close(connection);
		}
		return reservationStored;
	}

	/**
	 * Deletes a reservation from the database.
	 * 
	 * @param reservation The reservation to delete. The id of the reservation is used to determine which to delete.
	 * @return The deleted reservation or null if there was no reservation for the given id to delete.
	 * @throws ClassNotFoundException If a db driver can not be loaded.
	 * @throws SQLException On any database errors.
	 */
	public Reservation deleteReservation(Reservation reservation) throws ClassNotFoundException, SQLException {

		Connection connection = null;
		Reservation reservationDeleted = null;

		try {
			connection = dbConfig.getConnection();

			reservationDeleted = reservationDao.findByPrimaryKey(connection, reservation.getId());

			if (reservationDeleted != null) {

				int numberOfRowsAffected = reservationDao.deleteReservation(connection, reservation);

				if (numberOfRowsAffected == 0) {
					reservationDeleted = null;
				}
			}

		} finally {
			Common.close(connection);
		}

		return reservationDeleted;
	}

	/**
	 * Reschedules a reservation by deleting the old reservation (based on id) and creating a new reservation
	 * instead of it.
	 * 
	 * @param The oldReservation The old reservation to cancel.
	 * @param The newReservation The new created reservation.
	 * @return The old reservation null if unable to reschedule. 
	 * @throws SQLException On all database errors.
	 * @throws ClassNotFoundException If unable to load the database driver.
	 * @throws OverlappingReservationException If the newReservation overlaps with any existing reservation.
	 */
	public Reservation rescheduleReservation(Reservation oldReservation, Reservation newReservation) throws OverlappingReservationException, SQLException, ClassNotFoundException {

		Connection connection = dbConfig.getConnection();
		Reservation reservationCreated = null;

		try {
			connection.setAutoCommit(false);

			int numberOfRowsAffected = reservationDao.deleteReservation(connection, oldReservation);

			if (numberOfRowsAffected > 0) {

				List<Reservation> findOverlappingByDates = reservationDao.findOverlappingByDates(connection, newReservation.getResource(), newReservation.getStartTime(), newReservation.getEndTime());
				if (findOverlappingByDates.size() > 0) {
					throw new OverlappingReservationException();
				}

				reservationCreated = reservationDao.createReservation(connection, newReservation);
				connection.commit();
			}

		} catch (Exception e) {

			connection.rollback();
			connection.setAutoCommit(true);

			throw e;

		} finally {
			Common.close(connection);
		}

		return reservationCreated;
	}

}
