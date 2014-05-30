package mg.reservation.db;

import static mg.reservation.util.Common.close;
import static mg.reservation.validation.rule.ValidationRule.DATE_EARLIER;
import static mg.reservation.validation.rule.ValidationRule.NOT_NULL;
import static mg.reservation.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mg.reservation.validation.Validator;

public class ReservationDao {
	private static final String COL_ID = "id";
	private static final String COL_RESOURCE = "resource";
	private static final String COL_RESERVER = "reserver";
	private static final String COL_END_TIME = "end_time";
	private static final String COL_START_TIME = "start_time";
	private static final String COL_TITLE = "title";
	private static final String COL_DESCRIPTION = "description";
	private static final String OVERLAPPING_BETWEEN_DATES_SELECT = "SELECT * FROM reservations WHERE resource = ? and ? < end_time AND ? > start_time";
	private static final String INSERT = "INSERT INTO reservations (id, resource, reserver, start_time, end_time, title, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE = "DELETE FROM reservations WHERE id = ?";
	private static final String SELECT_BY_PRIMARY_KEY = "SELECT * FROM reservations WHERE id = ?";
	private static final String SELECT_STARTING_BY_DATE_RANGE = "SELECT * FROM reservations WHERE start_time >= ? and start_time <= ?";

	/**
	 * Finds and returns all reservations overlapping the given start and end times. <br />
	 * Note that a person can reserve several resources for events regardless will he attend those events.
	 * 
	 * @param connection The database connection to use.
	 * @param startTime the event start time.
	 * @param endTime the event end time.
	 * @throws IllegalArgumentException If any of the parameters were null.
	 * @throws SQLException On all sql errors.
	 * @return A list of reservations that overlap with the start and the end times.
	 */
	public List<Reservation> findOverlappingByDates(Connection connection, String resource, Date startTime, Date endTime) throws SQLException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("resource", resource, NOT_NULL_OR_EMPTY_STRING)
				.add("startTime", startTime, DATE_EARLIER.than(endTime))
				.validate();

		List<Reservation> reservations = new ArrayList<Reservation>();
		PreparedStatement betweenDatesStatement = null;
		try {
			betweenDatesStatement = connection.prepareStatement(OVERLAPPING_BETWEEN_DATES_SELECT);
			betweenDatesStatement.setString(1, resource);
			betweenDatesStatement.setTimestamp(2, new Timestamp(startTime.getTime()));
			betweenDatesStatement.setTimestamp(3, new Timestamp(endTime.getTime()));

			ResultSet resultSet = betweenDatesStatement.executeQuery();

			while (resultSet.next()) {

				Reservation reservation = new Reservation();
				reservation.setId(resultSet.getString(COL_ID));
				reservation.setResource(resultSet.getString(COL_RESOURCE));
				reservation.setResource(resultSet.getString(COL_RESERVER));
				reservation.setStartTime(resultSet.getDate(COL_START_TIME));
				reservation.setEndTime(resultSet.getDate(COL_END_TIME));
				reservation.setTitle(resultSet.getString(COL_TITLE));
				reservation.setDescription(resultSet.getString(COL_DESCRIPTION));
				reservations.add(reservation);
			}
		} finally {
			close(betweenDatesStatement);
		}

		return reservations;
	}

	/**
	 * Stores a given Reservation to the database by first verifying does it overlap with any existing reservations.
	 * 
	 * @param reservation the reservation to attempt to store to the database.
	 * @throws SQLException on all database errors.
	 * @throws IllegalArgumentException If any of the parameters were null.
	 * @return The given reservation if storing succeeded, returns null if failed.
	 */
	public Reservation createReservation(Connection connection, Reservation reservation) throws SQLException, IllegalArgumentException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("reservation", reservation, NOT_NULL)
				.validate();

		PreparedStatement insertStatement = null;
		try {
			Timestamp startTime = new Timestamp(reservation.getStartTime().getTime());
			Timestamp endTime = new Timestamp(reservation.getEndTime().getTime());

			insertStatement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
			insertStatement.setString(1, reservation.getId());
			insertStatement.setString(2, reservation.getResource());
			insertStatement.setString(3, reservation.getReserver());
			insertStatement.setTimestamp(4, startTime);
			insertStatement.setTimestamp(5, endTime);
			insertStatement.setString(6, reservation.getTitle());
			insertStatement.setString(7, reservation.getDescription());

			int numberOfRowsAffected = insertStatement.executeUpdate();

			if (numberOfRowsAffected > 0) {
				return reservation;
			}

		} finally {
			close(insertStatement);
		}

		return null;
	}

	/**
	 * Removes a given reservation from the database.
	 * 
	 * @param reservation The reservation to be removed.
	 * @throws SQLException on all database errors.
	 * @throws IllegalArgumentException If any of the parameters were null.
	 * @return 1 if the removal was successful, 0 otherwise.
	 */
	public int deleteReservation(Connection connection, Reservation reservation) throws ClassNotFoundException, SQLException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("reservation", reservation, NOT_NULL)
				.validate();

		PreparedStatement deletionStatement = null;

		try {
			deletionStatement = connection.prepareStatement(DELETE);
			deletionStatement.setString(1, reservation.getId());

			int numberOfRowsAffected = deletionStatement.executeUpdate();

			return numberOfRowsAffected;

		} finally {
			close(deletionStatement);
		}
	}

	/**
	 * Fetches an reservation based on an id.
	 * 
	 * @param connection The connection to use with the statement.
	 * @param id the primary key of the reservation to query for.
	 * @return if successful: the Reservation corresponding to the id, otherwise a null.
	 * @throws SQLException on all db errors.
	 * @throws IllegalArgumentException If any of the parameters were null.
	 */
	public Reservation findByPrimaryKey(Connection connection, String id) throws SQLException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("id", id, NOT_NULL_OR_EMPTY_STRING)
				.validate();

		PreparedStatement findStatement = null;
		Reservation reservation = null;
		try {
			findStatement = connection.prepareStatement(SELECT_BY_PRIMARY_KEY);
			findStatement.setString(1, id);

			ResultSet resultSet = findStatement.executeQuery();

			if (resultSet.next()) {
				reservation = getReservationFromResultSet(resultSet);
			}

			return reservation;

		} finally {
			close(findStatement);
		}
	}

	private Reservation getReservationFromResultSet(ResultSet resultSet) throws SQLException {
		Reservation reservation = new Reservation();
		reservation.setId(resultSet.getString(COL_ID));
		reservation.setResource(resultSet.getString(COL_RESOURCE));
		reservation.setReserver(resultSet.getString(COL_RESERVER));
		reservation.setStartTime(resultSet.getTimestamp(COL_START_TIME));
		reservation.setEndTime(resultSet.getTimestamp(COL_END_TIME));
		reservation.setTitle(resultSet.getString(COL_TITLE));
		reservation.setDescription(resultSet.getString(COL_DESCRIPTION));
		return reservation;
	}

	/**
	 * Fetches all reservations that start between start and end times.
	 * 
	 * @param connection The connection to use with the statement.
	 * @param startTime The low boundary for reservations to fetch.
	 * @param endTime The high boundary for reservations to fetch.
	 * @return if successful: the Reservation corresponding to the id, otherwise a null.
	 * @throws SQLException on all db errors.
	 * @throws IllegalArgumentException If any of the parameters were null.
	 */
	public List<Reservation> findByDates(Connection connection, Date startTime, Date endTime) throws SQLException {

		new Validator()
				.add("connection", connection, NOT_NULL)
				.add("startTime", startTime, NOT_NULL)
				.add("endTime", endTime, NOT_NULL)
				.validate();

		List<Reservation> reservations = new ArrayList<Reservation>();
		PreparedStatement findStatement = null;
		try {

			Timestamp startTimeStamp = new Timestamp(startTime.getTime());
			Timestamp endTimeStamp = new Timestamp(endTime.getTime());

			findStatement = connection.prepareStatement(SELECT_STARTING_BY_DATE_RANGE);
			findStatement.setTimestamp(1, startTimeStamp);
			findStatement.setTimestamp(2, endTimeStamp);

			ResultSet resultSet = findStatement.executeQuery();

			while (resultSet.next()) {

				Reservation reservation = getReservationFromResultSet(resultSet);

				reservations.add(reservation);
			}

		} finally {
			close(findStatement);
		}

		return reservations;

	}

}
