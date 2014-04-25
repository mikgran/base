package mg.reservation.application;

import mg.reservation.page.Reservation;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

public class ReservationApplication extends WebApplication {
	
	public ReservationApplication() {
	}

	@Override
	public void init()
	{
		super.init();
		this.getMarkupSettings().setStripWicketTags(true);
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return Reservation.class;
	}
}
