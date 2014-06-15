package mg.reservation.panel;

import static mg.reservation.util.Common.yyyyMMddHHmmFormatter;

import java.sql.SQLException;
import java.text.ParseException;
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

	private static final long serialVersionUID = 830442203196048049L;
	private static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
	private Logger logger = LoggerFactory.getLogger(ReservationsPanel.class);

	@Inject
	private ReservationService reservationService;

	public ReservationsPanel(String id) {
		super(id);

		List<Reservation> reservations = loadReservations();

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

	private List<Reservation> loadReservations() {
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
