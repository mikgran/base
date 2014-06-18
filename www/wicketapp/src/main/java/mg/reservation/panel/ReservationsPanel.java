package mg.reservation.panel;

import mg.reservation.db.Reservation;
import mg.reservation.model.ReservationsModel;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

public class ReservationsPanel extends Panel {

	private static final long serialVersionUID = 830442203196048049L;
	private static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
	private ReservationsModel reservationsModel;

	public ReservationsPanel(String id, ReservationsModel reservationsModel) {
		super(id);
		this.reservationsModel = reservationsModel;

		add(getReservationsListView(reservationsModel));
	}

	private PropertyListView<Reservation> getReservationsListView(ReservationsModel reservationsModel) {
		return new PropertyListView<Reservation>("reservations", reservationsModel) {

			private static final long serialVersionUID = -8328953506175828323L;

			@Override
			protected void populateItem(final ListItem<Reservation> item) {

				CompoundPropertyModel<Reservation> model = new CompoundPropertyModel<Reservation>(item.getModel());
				item.setModel(model);
				item.add(new Label("id"));
				item.add(new Label("title"));
				item.add(new DateLabel("start", new PatternDateConverter(DD_MM_YYYY_HH_MM, false)));
				item.add(new DateLabel("end", new PatternDateConverter(DD_MM_YYYY_HH_MM, false)));

				item.add(new AjaxEventBehavior("onclick") {
					private static final long serialVersionUID = 2856171987289507739L;

					@Override
					protected void onEvent(AjaxRequestTarget target) {
						Reservation r = item.getModelObject();
						// setResponsePage(null);
					}
				});

			}
		};
	}

	@Override
	public boolean isVisible() {
		return reservationsModel.getObject().size() > 0;
	}

}
