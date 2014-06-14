package mg.reservation.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

// accessed via <listener-class>mg.reservation.config.ReservationServletConfig</listener-class> in pom.xml
public class ReservationServletConfig extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ReservationServletModule());
	}
}
