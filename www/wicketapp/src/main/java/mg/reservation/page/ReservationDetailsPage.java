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

	/**
	 * A page with a reservation already loaded.
	 * 
	 * @param parameters The page parameters.
	 * @param reservationsModel The reservations model.
	 * @param reservation The selected reservation to be displayed
	 */
	public ReservationDetailsPage(final PageParameters parameters,
			ReservationsModel reservationsModel,
			IModel<Reservation> reservation) {

		super(parameters);
		this.model = reservation;

		reservationDetailsPanel = new ReservationDetailsPanel("reservationDetails", model);

		add(reservationDetailsPanel);
	}

	/**
	 * A page for creating a new reservation to the model.
	 * 
	 * @param parameters The page parameters.
	 * @param reservationsModel The reservations model.
	 */
	public ReservationDetailsPage(final PageParameters parameters,
			ReservationsModel reservationsMode) {

		super(parameters);

		reservationDetailsPanel = new ReservationDetailsPanel("reservationDetails", model);

		add(reservationDetailsPanel);
	}

}
