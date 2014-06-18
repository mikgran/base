package mg.reservation.page;

import mg.reservation.db.Reservation;
import mg.reservation.model.ReservationsModel;
import mg.reservation.panel.ReservationDetailsPanel;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ReservationDetailsPage extends WebPage {

	private static final long serialVersionUID = 6328169921771405077L;

	private IModel<Reservation> model;

	private ReservationDetailsPanel reservationDetailsPanel;

	public ReservationDetailsPage(final PageParameters parameters,
			ReservationsModel reservationsModel,
			IModel<Reservation> reservation) {

		super(parameters);
		this.model = reservation;

		reservationDetailsPanel = new ReservationDetailsPanel("reservationDetails", model);

		add(reservationDetailsPanel);
	}

}
