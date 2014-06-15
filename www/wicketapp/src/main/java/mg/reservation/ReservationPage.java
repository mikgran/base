package mg.reservation;

import mg.reservation.panel.ReservationsPanel;
import mg.reservation.panel.WeekSelectPanel;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ReservationPage extends WebPage {

	private static final long serialVersionUID = -7385489983766355838L;

	private ReservationsPanel reservationsPanel;
	private WeekSelectPanel weekSelectPanel;

	public ReservationPage(final PageParameters parameters) {
		super(parameters);

		reservationsPanel = new ReservationsPanel("reservationsList");
		weekSelectPanel = new WeekSelectPanel("weekSelect");

		add(reservationsPanel);
		add(weekSelectPanel);
	}
}
