package mg.reservation.dao;

import static mg.reservation.validation.rule.ValidationRule.DATE_EARLIER;
import static mg.reservation.validation.rule.ValidationRule.NOT_EMPTY_STRING;
import static mg.reservation.validation.rule.ValidationRule.NOT_NULL;
import static mg.reservation.validation.rule.ValidationRule.NOT_NEGATIVE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
	private static final String RESERVATION_SELECT_BY_PRIMARY_KEY = "SELECT * FROM reservations WHERE id = ?";

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
	 * @throws IllegalArgumentException
	 *             If any of the parameters are null.
	 * @throws SQLException
	 *             On all sql errors.
	 * @return A list of reservations that overlap with the start and the end times.
	 */
	public List<Reservation> findOverlappingByDates(Connection connection, String resource, Date startTime, Date endTime) throws SQLException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("resource", resource, NOT_EMPTY_STRING)
				.add("startTime", startTime, DATE_EARLIER.than(endTime))
				.validate();

		List<Reservation> reservations = new ArrayList<Reservation>();
		PreparedStatement betweenDatesStatement = null;
		try {
			betweenDatesStatement = connection.prepareStatement(ALL_BETWEEN_DATES_SELECT);
			betweenDatesStatement.setString(1, resource);
			betweenDatesStatement.setDate(2, new java.sql.Date(startTime.getTime()));
			betweenDatesStatement.setDate(3, new java.sql.Date(endTime.getTime()));

			ResultSet resultSet = betweenDatesStatement.executeQuery();

			while (resultSet.next()) {

				Reservation reservation = new Reservation();
				reservation.setId(resultSet.getLong(COL_ID));
				reservation.setResource(resultSet.getString(COL_RESOURCE));
				reservation.setResource(resultSet.getString(COL_RESERVER));
				reservation.setStartTime(resultSet.getDate(COL_START_TIME));
				reservation.setEndTime(resultSet.getDate(COL_END_TIME));

				reservations.add(reservation);
			}
		} finally {
			Common.close(betweenDatesStatement);
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
	 *             If any of the parameters are null.
	 * @return The given reservation with filled autoincremented key (id) if storing succeeded, returns null if failed.
	 */
	public Reservation storeReservation(Connection connection, Reservation reservation) throws SQLException, OverlappingReservationException, IllegalArgumentException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("reservation", reservation, NOT_NULL)
				.validate();

		PreparedStatement insertStatement = null;
		try {

			List<Reservation> overlappingReservations = findOverlappingByDates(connection, reservation.getResource(), reservation.getStartTime(), reservation.getEndTime());
			if (overlappingReservations.size() > 0) {
				throw new OverlappingReservationException();
			}

			Timestamp startTime = new Timestamp(reservation.getStartTime().getTime());
			Timestamp endTime = new Timestamp(reservation.getEndTime().getTime());

			insertStatement = connection.prepareStatement(RESERVATION_INSERT, Statement.RETURN_GENERATED_KEYS);
			insertStatement.setString(1, reservation.getResource());
			insertStatement.setString(2, reservation.getReserver());
			insertStatement.setTimestamp(3, startTime);
			insertStatement.setTimestamp(4, endTime);
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

	/**
	 * Removes a given reservation from the database.
	 * 
	 * @param reservation
	 *            The reservation to be removed.
	 * @throws SQLException
	 *             on all database errors.
	 * @throws IllegalArgumentException
	 *             If any of the parameters are null.
	 * @return 1 if the removal was successful, 0 otherwise.
	 */
	public int deleteReservation(Connection connection, Reservation reservation) throws ClassNotFoundException, SQLException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("reservation", reservation, NOT_NULL)
				.validate();

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

	/**
	 * Fetches an reservation based on an id.
	 * 
	 * @param connection
	 *            The connection to use with the statement.
	 * @param id
	 *            the primary key of the reservation to query for.
	 * @return if successful: the Reservation corresponding to the id, otherwise a null.
	 * @throws SQLException
	 *             on all db errors.
	 */
	public Reservation findByPrimaryKey(Connection connection, long id) throws SQLException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("id", id, NOT_NEGATIVE)
				.validate();

		PreparedStatement findStatement = null;
		Reservation reservation = null;
		try {
			findStatement = connection.prepareStatement(RESERVATION_SELECT_BY_PRIMARY_KEY);
			findStatement.setLong(1, id);

			ResultSet resultSet = findStatement.executeQuery();

			if (resultSet.next()) {
				reservation = new Reservation();
				reservation.setId(resultSet.getLong(COL_ID));
				reservation.setResource(resultSet.getString(COL_RESOURCE));
				reservation.setReserver(resultSet.getString(COL_RESERVER));
				reservation.setStartTime(resultSet.getTimestamp(COL_START_TIME));
				reservation.setEndTime(resultSet.getTimestamp(COL_END_TIME));
			}

			return reservation;

		} finally {
			Common.close(findStatement);
		}
	}

}
