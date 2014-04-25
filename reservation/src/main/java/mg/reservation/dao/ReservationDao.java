package mg.reservation.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservationDao {

	private static final String COL_ID = "id";
	private static final String COL_RESOURCE = "resource";
	private static final String COL_RESERVER = "reserver";
	private static final String COL_END_TIME = "end_time";
	private static final String COL_START_TIME = "start_time";
	// TODO replace these with configuration file fetch:
	private static final String USER_NAME = "testuser";
	private static final String PASSWORD = "testpass";
	private static final String DB_URL = "jdbc:mysql://localhost/reservation";
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String ALL_BETWEEN_DATES_SELECT = "SELECT * FROM reservation.reservations WHERE resource = ? and ? < end_time AND ? > start_time";
	private static final String RESERVATION_INSERT = "INSERT INTO reservation.reservations (resource, reserver, start_time, end_time, description) VALUES (?, ?, ?, ?, ?)";
	private static final String RESERVATION_DELETE = "DELETE FROM reservation.reservations WHERE id = ?";

	/**
	 * Finds and returns all reservations overlapping the given start and end times.
	 * 
	 * @param startTime
	 *            the event start time.
	 * @param endTime
	 *            the event end time.
	 * @return A list of reservations that overlap with the start and end times.
	 * @throws SQLException
	 *             On all sql errors.
	 * @throws ClassNotFoundException
	 *             If database driver loadup fails.
	 */
	public List<Reservation> findOverlappingByDates(String resource, Date startTime, Date endTime) throws SQLException, ClassNotFoundException {

		if (resource == null || resource.length() == 0 || startTime == null || endTime == null || startTime.getTime() > endTime.getTime()) {
			throw new IllegalArgumentException(String.format("Invalid arguments: resource: %s, startTime: %s, endTime %s.", resource, startTime, endTime));
		}

		List<Reservation> reservations = new ArrayList<Reservation>();

		Connection connection = getConnection();

		PreparedStatement preparedStatement = connection.prepareStatement(ALL_BETWEEN_DATES_SELECT); // note that a person can reserve several resources for events regardless will he attend those events.
		preparedStatement.setString(1, resource);
		preparedStatement.setDate(2, new java.sql.Date(startTime.getTime()));
		preparedStatement.setDate(3, new java.sql.Date(endTime.getTime()));

		ResultSet resultSet = preparedStatement.executeQuery();

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
	 * @return 1 if successful, 0 otherwise.
	 * @throws ClassNotFoundException
	 *             if db driver loadup fails.
	 * @throws SQLException
	 *             on all db errors.
	 * @throws OverlappingReservationException
	 *             If one ore more existing reservations overlap with the given reservation.
	 * @throws IllegalArgumentException
	 *             If reservation is null.
	 */
	public int storeReservation(Reservation reservation) throws ClassNotFoundException, SQLException, OverlappingReservationException, IllegalArgumentException {

		if (reservation == null) {
			throw new IllegalArgumentException("reservation can not be null.");
		}

		List<Reservation> overlappingReservations = findOverlappingByDates(reservation.getResource(), reservation.getStartTime(), reservation.getEndTime());
		if (overlappingReservations.size() > 0) {
			throw new OverlappingReservationException();
		}

		Connection connection = getConnection();

		PreparedStatement prepareStatement = connection.prepareStatement(RESERVATION_INSERT);
		prepareStatement.setString(1, reservation.getResource());
		prepareStatement.setString(2, reservation.getReserver());
		prepareStatement.setDate(3, new java.sql.Date(reservation.getStartTime().getTime()));
		prepareStatement.setDate(4, new java.sql.Date(reservation.getStartTime().getTime()));
		prepareStatement.setString(5, reservation.getDescription());

		return prepareStatement.executeUpdate();
	}

	private Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(DB_DRIVER);
		Connection connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
		return connection;
	}
	
	public int deleteReservation(Reservation reservation) throws ClassNotFoundException, SQLException {
		
		if (reservation == null) {
			throw new IllegalArgumentException("reservation can not be null.");
		}
		
		Connection connection = getConnection();
		
		connection.prepareStatement(RESERVATION_DELETE);
		
		return 0;
	}

}
