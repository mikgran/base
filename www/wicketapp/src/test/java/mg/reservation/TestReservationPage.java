package mg.reservation;

import mg.reservation.config.ReservationServletModule;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestReservationPage {

	private WicketTester tester;

	@Before
	public void setUp() {
		Injector injector = Guice.createInjector(new ReservationServletModule());
		tester = new WicketTester(new WicketApplication(injector));
	}

	@Test
	public void reservationPageRendersSuccessfully() {

		// start and render the test page
		tester.startPage(ReservationPage.class);

		// assert rendered page class
		tester.assertRenderedPage(ReservationPage.class);
	}
}
