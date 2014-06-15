package mg.reservation.panel;

import mg.reservation.db.Reservation;
import mg.reservation.model.ReservationsModel;
import mg.reservation.service.ReservationService;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import com.google.inject.Inject;

public class ReservationsPanel extends Panel {

	private static final long serialVersionUID = 830442203196048049L;
	private static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";

	private ListView<Reservation> reservationsListView;
	private WebMarkupContainer webMarkupContainer;

	@Inject
	private ReservationService reservationService;

	public ReservationsPanel(String id) {
		super(id);

		reservationsListView = getReservationsListView(new ReservationsModel(reservationService));

		webMarkupContainer = new WebMarkupContainer("reservationsParent");
		webMarkupContainer.add(reservationsListView);

		this.setOutputMarkupId(true);
		add(webMarkupContainer);
	}

	private PropertyListView<Reservation> getReservationsListView(ReservationsModel reservationsModel) {
		return new PropertyListView<Reservation>("reservations", reservationsModel) {

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
}
