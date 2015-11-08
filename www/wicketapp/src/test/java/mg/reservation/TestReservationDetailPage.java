package mg.reservation;

import mg.reservation.config.WicketApplication;
import mg.reservation.db.Reservation;
import mg.reservation.model.ReservationsModel;
import mg.reservation.page.ReservationDetailsPage;
import mg.reservation.service.ReservationService;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class TestReservationDetailPage {

	private WicketTester tester;

	@Inject
	private ReservationService reservationService;

	@Before
	public void setUp() {
		Injector injector = Guice.createInjector(new ReservationServletTestModule());
		tester = new WicketTester(new WicketApplication(injector));
	}

	@Test
	public void reservationDetailPageRendersSuccessfully() throws InterruptedException {

		tester.startPage(new ReservationDetailsPage(new PageParameters(),
				new ReservationsModel(reservationService),
				new Model<Reservation>(new Reservation())));

		tester.assertRenderedPage(ReservationDetailsPage.class);
		
		// TOIMPROVE a test which navigates from main page to details page: 
		
		// FormTester formTester = tester.newFormTester("parent:weekSelect:weekSelection");
		// formTester.setValue("week", "24");
		//
		// tester.executeAjaxEvent("parent:weekSelect:weekSelection:ajaxSubmit", "onclick");
		//
		// PropertyListView<Reservation> reservations = (PropertyListView<Reservation>) tester.getComponentFromLastRenderedPage("parent:reservationsList:reservations", false);
		// assertTrue("reservations should be visible", reservations.isVisible());
		//
		// System.out.println("relapath: " + reservations.getPageRelativePath());
		// System.out.println("reservations size: " + reservations.size());

		// tester.executeAjaxEvent("parent:reservationsList:reservations:0:id", "onclick");

		// tester.assertRenderedPage(ReservationListPage.class);
	}
}
