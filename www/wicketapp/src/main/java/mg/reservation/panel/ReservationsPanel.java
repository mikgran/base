package mg.reservation.panel;

import static mg.reservation.util.Common.yyyyMMddHHmmFormatter;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import mg.reservation.db.Reservation;
import mg.reservation.service.ReservationService;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ReservationsPanel extends Panel {

	private static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
	private Logger logger = LoggerFactory.getLogger(ReservationsPanel.class);

	@Inject
	private ReservationService reservationService;

	public ReservationsPanel(String id) {
		super(id);

		List<Reservation> reservations = loadReservations2();

		add(getReservationsListView(reservations));

	}

	private ListView<Reservation> getReservationsListView(List<Reservation> reservations) {
		return new ListView<Reservation>("reservations", reservations) {

			private static final long serialVersionUID = -8328953506175828323L;

			@Override
			protected void populateItem(ListItem<Reservation> item) {

				CompoundPropertyModel<Reservation> model = new CompoundPropertyModel<Reservation>(item.getModel());
				item.setModel(model);
				item.add(new Label("id"));
				item.add(new Label("title"));
				item.add(new DateLabel("start", new PatternDateConverter(DD_MM_YYYY_HH_MM, false)));
				item.add(new DateLabel("end", new PatternDateConverter(DD_MM_YYYY_HH_MM, false)));
			}
		};
	}

	private List<Reservation> loadReservations2() {
		List<Reservation> reservations = null;
		try {

			reservations = reservationService.findReservations(new Date(), new Date());

		} catch (ClassNotFoundException | SQLException e) {

			logger.info("exception: ", e);
		}
		return reservations;
	}

	private static final long serialVersionUID = 830442203196048049L;

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
