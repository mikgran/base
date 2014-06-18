package mg.reservation.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mg.reservation.db.Reservation;
import mg.reservation.service.ReservationService;
import mg.reservation.util.Common;

import org.apache.wicket.model.LoadableDetachableModel;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationsModel extends LoadableDetachableModel<List<Reservation>> {

	private ReservationService reservationService;
	private static final long serialVersionUID = 5788711624040388316L;
	private Logger logger = LoggerFactory.getLogger(ReservationsModel.class);
	private int selectedWeek = 0;

	public ReservationsModel(ReservationService reservationService) {
		this.reservationService = reservationService;

		selectedWeek = getCurrentWeekNumber();
	}

	@Override
	protected List<Reservation> load() {

		List<Reservation> reservations = null;
		try {
			logger.info("load() selected week: {}.", getSelectedWeek());

			Date date = new Date();
			Date weekStart = Common.getFirstInstantOfTheWeek(date, getSelectedWeek());
			Date weekEnd = Common.getLastInstantOfTheWeek(date, getSelectedWeek());

			// TODO: make refreshing and dynamic instead of static range.
			reservations = reservationService.findReservations(weekStart, weekEnd);

		} catch (SQLException | ClassNotFoundException e) {

			logger.error("exception while trying to load reservation model: ", e);

			// allowing the database exception or driver loading exception to break the program
			throw new RuntimeException("No database driver or configuration found.");
		}

		return reservations;
	}

	public int getSelectedWeek() {
		return selectedWeek;
	}

	public void setSelectedWeek(int selectedWeek) {
		this.selectedWeek = selectedWeek;
	}

	public String getSelectedWeekAsString() {
		return "" + getSelectedWeek();
	}

	public void setWeek(String week) {
		try {
			selectedWeek = Integer.parseInt(week);
		} catch (NumberFormatException e) {
			logger.warn("Unable to parse week: {}.", week);
		}
	}

	private int getCurrentWeekNumber() {
		return new DateTime().weekOfWeekyear().get();
	}

}
