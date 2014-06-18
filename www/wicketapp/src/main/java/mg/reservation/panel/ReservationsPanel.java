package mg.reservation.panel;

import mg.reservation.db.Reservation;
import mg.reservation.model.ReservationsModel;
import mg.reservation.page.ReservationDetailPage;
import mg.reservation.util.Common;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationsPanel extends Panel {

	private Logger logger = LoggerFactory.getLogger(ReservationsPanel.class);
	private static final long serialVersionUID = 830442203196048049L;
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
				item.add(new DateLabel("start", new PatternDateConverter(Common.DD_MM_YYYY_HH_MM, false)));
				DateLabel dateLabel = new DateLabel("end", new PatternDateConverter(Common.DD_MM_YYYY_HH_MM, false));
				item.add(dateLabel);

				item.add(new AjaxEventBehavior("onclick") {
					private static final long serialVersionUID = 2856171987289507739L;

					@Override
					protected void onEvent(AjaxRequestTarget target) {

						logger.debug("AjaxEventBehavior(\"onclick\") onEvent() setResponsePage(ReservationDetailPage)");

						setResponsePage(new ReservationDetailPage(new PageParameters(),
								getReservationsModel(),
								new Model<Reservation>(item.getModelObject())));
					}

				});

			}
		};
	}

	private ReservationsModel getReservationsModel() {
		return this.reservationsModel;
	}

	@Override
	public boolean isVisible() {
		boolean isVisible = reservationsModel.getObject().size() > 0;
		logger.debug("ReservationsPanel isVisible(): {}", isVisible);
		return isVisible;
	}

}
