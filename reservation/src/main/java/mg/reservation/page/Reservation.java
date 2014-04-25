package mg.reservation.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class Reservation extends WebPage {

	private static final long serialVersionUID = -3102962554136548565L;

	public Reservation() {

		add(new Label("message", "Message"));
	}

}