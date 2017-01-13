package mg.reservation;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import mg.reservation.config.WicketApplication;
import mg.reservation.page.ReservationListPage;

@Ignore
public class TestReservationListPage {

	private WicketTester tester;

	@Test
	public void reservationListPageRendersSuccessfully() {

		tester.startPage(ReservationListPage.class);

		tester.assertRenderedPage(ReservationListPage.class);
	}

	@Before
	public void setUp() {
		Injector injector = Guice.createInjector(new ReservationServletTestModule());
		tester = new WicketTester(new WicketApplication(injector));
	}
}
