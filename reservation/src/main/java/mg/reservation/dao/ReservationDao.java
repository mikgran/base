package mg.reservation.dao;

import static mg.reservation.util.Common.isAnyNull;
import static mg.reservation.validation.rule.ValidationRule.NOT_EMPTY_STRING;
import static mg.reservation.validation.rule.ValidationRule.NOT_NULL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mg.reservation.util.Common;
import mg.reservation.validation.Validator;

public class ReservationDao {

	private static final String COL_ID = "id";
	private static final String COL_RESOURCE = "resource";
	private static final String COL_RESERVER = "reserver";
	private static final String COL_END_TIME = "end_time";
	private static final String COL_START_TIME = "start_time";
	private static final String ALL_BETWEEN_DATES_SELECT = "SELECT * FROM reservations WHERE resource = ? and ? < end_time AND ? > start_time";
	private static final String RESERVATION_INSERT = "INSERT INTO reservations (resource, reserver, start_time, end_time, description) VALUES (?, ?, ?, ?, ?)";
	private static final String RESERVATION_DELETE = "DELETE FROM reservations WHERE id = ?";

	/**
	 * Finds and returns all reservations overlapping the given start and end times. <br />
	 * Note that a person can reserve several resources for events regardless will he attend those events.
	 * 
	 * @param connection
	 *            The database connection to use.
	 * @param startTime
	 *            the event start time.
	 * @param endTime
	 *            the event end time.
	 * @throws SQLException
	 *             On all sql errors.
	 * @return A list of reservations that overlap with the start and the end times.
	 */
	public List<Reservation> findOverlappingByDates(Connection connection, String resource, Date startTime, Date endTime) throws SQLException {

		if (connection == null || resource == null || resource.length() == 0 || startTime == null || endTime == null || startTime.getTime() > endTime.getTime()) {
			String message = String.format("Invalid arguments: resource: %s, startTime: %s, endTime: %s, connection: %s.", resource, startTime, endTime, connection);
			throw new IllegalArgumentException(message);
		}

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("resource", resource, NOT_EMPTY_STRING)
				.validate();

		List<Reservation> reservations = new ArrayList<Reservation>();

		PreparedStatement statement = connection.prepareStatement(ALL_BETWEEN_DATES_SELECT);
		statement.setString(1, resource);
		statement.setDate(2, new java.sql.Date(startTime.getTime()));
		statement.setDate(3, new java.sql.Date(endTime.getTime()));

		ResultSet resultSet = statement.executeQuery();

		while (resultSet.next()) {

			Reservation reservation = new Reservation();
			reservation.setId(resultSet.getLong(COL_ID));
			reservation.setResource(resultSet.getString(COL_RESOURCE));
			reservation.setResource(resultSet.getString(COL_RESERVER));
			reservation.setStartTime(resultSet.getDate(COL_START_TIME));
			reservation.setEndTime(resultSet.getDate(COL_END_TIME));

			reservations.add(reservation);
		}

		return reservations;
	}

	/**
	 * Stores a given Reservation to the database by first verifying does it overlap with any existing reservations.
	 * 
	 * @param reservation
	 *            the reservation to attempt to store to the database.
	 * @throws SQLException
	 *             on all database errors.
	 * @throws OverlappingReservationException
	 *             If one ore more existing reservations overlap with the given reservation.
	 * @throws IllegalArgumentException
	 *             If reservation is null.
	 * @return The given reservation with filled autoincremented key (id) if storing succeeded, returns null if failed.
	 */
	public Reservation storeReservation(Connection connection, Reservation reservation) throws SQLException, OverlappingReservationException, IllegalArgumentException {

		if (isAnyNull(connection, reservation)) {
			String message = makeInvalidArgumentsMessage(connection, reservation);
			throw new IllegalArgumentException(message);
		}

		PreparedStatement insertStatement = null;
		try {

			List<Reservation> overlappingReservations = findOverlappingByDates(connection, reservation.getResource(), reservation.getStartTime(), reservation.getEndTime());
			if (overlappingReservations.size() > 0) {
				throw new OverlappingReservationException();
			}

			insertStatement = connection.prepareStatement(RESERVATION_INSERT, Statement.RETURN_GENERATED_KEYS);
			insertStatement.setString(1, reservation.getResource());
			insertStatement.setString(2, reservation.getReserver());
			insertStatement.setDate(3, new java.sql.Date(reservation.getStartTime().getTime()));
			insertStatement.setDate(4, new java.sql.Date(reservation.getStartTime().getTime()));
			insertStatement.setString(5, reservation.getDescription());

			int numberOfRowsAffected = insertStatement.executeUpdate();

			if (numberOfRowsAffected > 0) {
				reservation.setId(fetchAutoIcrementedId(insertStatement));
				return reservation;
			}

		} finally {
			Common.close(insertStatement);
		}

		return null;
	}

	private int fetchAutoIcrementedId(PreparedStatement insertStatement) throws SQLException {
		int autoIncKeyFromApi = -1;
		ResultSet rs = insertStatement.getGeneratedKeys();
		if (rs.next()) {
			autoIncKeyFromApi = rs.getInt(1);
		} else {
			throw new SQLException("Unable to get generated keys from statement.");
		}
		return autoIncKeyFromApi;
	}

	private String makeInvalidArgumentsMessage(Connection connection, Reservation reservation) {
		String message = String.format("Invalid arguments: connection: %s, reservation: %s.", connection, reservation);
		return message;
	}

	/**
	 * Removes a given reservation from the database.
	 * 
	 * @param reservation
	 *            The reservation to be removed.
	 * @throws SQLException
	 *             on all database errors.
	 * @return 1 if the removal was successful, 0 otherwise.
	 */
	public int deleteReservation(Connection connection, Reservation reservation) throws ClassNotFoundException, SQLException {

		if (reservation == null || connection == null) {
			throw new IllegalArgumentException("reservation can not be null.");
		}

		PreparedStatement deletionStatement = null;

		try {
			deletionStatement = connection.prepareStatement(RESERVATION_DELETE);
			deletionStatement.setLong(1, reservation.getId());

			int numberOfRowsAffected = deletionStatement.executeUpdate();

			return numberOfRowsAffected;

		} finally {
			Common.close(deletionStatement);
		}
	}

}
