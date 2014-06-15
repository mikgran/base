package mg.reservation.model;

import static mg.reservation.util.Common.yyyyMMddHHmmFormatter;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import mg.reservation.db.Reservation;
import mg.reservation.service.ReservationService;

import org.apache.wicket.model.LoadableDetachableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationsModel extends LoadableDetachableModel<List<Reservation>> {

	private ReservationService reservationService;
	private static final long serialVersionUID = 5788711624040388316L;
	private Logger logger = LoggerFactory.getLogger(ReservationsModel.class);
	

	public ReservationsModel(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@Override
	protected List<Reservation> load() {

		List<Reservation> reservations = null;
		try {

			// TODO: make refreshing and dynamic instead of static range.
			reservations = reservationService.findReservations(dateFrom("2014-06-11 08:00"), dateFrom("2014-06-13 13:00"));

		} catch (SQLException | ParseException e) {

			logger.info("exception: ", e); // TODO meaningful logging, etc

		} catch (ClassNotFoundException e) {

			logger.info("exception: ", e); // TODO meaningful logging, etc, allow the missing DB classes to break the program.
			throw new RuntimeException("No database driver or configuration found.");
		}

		return reservations;
	}

	private Date dateFrom(String dateString) throws ParseException {
		return yyyyMMddHHmmFormatter.parse(dateString);
	}

}
