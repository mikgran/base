package mg.reservation;

import static mg.reservation.util.Common.yyyyMMddHHmmFormatter;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import mg.reservation.db.Reservation;
import mg.reservation.panel.ReservationsPanel;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ReservationPage extends WebPage {

	private static final long serialVersionUID = -7385489983766355838L;

	public ReservationPage(final PageParameters parameters) {
		super(parameters);

		List<Reservation> reservations = loadReservations();

		add(new ReservationsPanel("mainPanel", reservations));
	}

	// TODO replace with db search
	private List<Reservation> loadReservations() {
		try {
			return Arrays.asList(
					reservationFrom("A", "Beta", "person", "2014-06-12 10:00", "2014-06-12 11:00", "title1", "desc1"),
					reservationFrom("B", "Beta", "person", "2014-06-12 12:00", "2014-06-12 13:00", "title2", "desc2"));
		} catch (Exception e) {
		}
		return null;
	}

	private Reservation reservationFrom(String id, String resource, String reserver, String startTimeString, String endTimeString, String title, String description) throws ParseException {
		return new Reservation(id, resource, reserver, dateFrom(startTimeString), dateFrom(endTimeString), title, description);
	}

	private Date dateFrom(String dateString) throws ParseException {
		return yyyyMMddHHmmFormatter.parse(dateString);
	}

}
