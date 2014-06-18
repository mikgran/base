package mg.reservation.config;

import mg.reservation.page.ReservationListPage;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see mg.reservation.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{

	private final Injector injector;

	@Inject
	public WicketApplication(Injector injector) {
		this.injector = injector;
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage() {
		return ReservationListPage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init() {
		super.init();

		getComponentInstantiationListeners().add(new GuiceComponentInjector(this, injector));
	}
}
