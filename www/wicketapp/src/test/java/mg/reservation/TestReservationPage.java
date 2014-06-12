package mg.reservation;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class TestReservationPage
{
	private WicketTester tester;

	@Before
	public void setUp()
	{
		tester = new WicketTester(new WicketApplication());
	}

	@Test
	public void reservationPageRendersSuccessfully()
	{
		// start and render the test page
		tester.startPage(ReservationPage.class);

		// assert rendered page class
		tester.assertRenderedPage(ReservationPage.class);
	}
}
