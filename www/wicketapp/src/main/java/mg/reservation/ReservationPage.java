package mg.reservation;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ReservationPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public ReservationPage(final PageParameters parameters) {
		super(parameters);

		add(new Label("message", "Hello world"));

		// TODO Add your page's components here

    }
}
