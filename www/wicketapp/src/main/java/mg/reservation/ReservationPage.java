package mg.reservation;

import mg.reservation.panel.ReservationsPanel;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ReservationPage extends WebPage {

	private static final long serialVersionUID = -7385489983766355838L;

	public ReservationPage(final PageParameters parameters) {
		super(parameters);

		add(new ReservationsPanel("mainPanel"));
	}
}
