package mg.reservation.page;

import mg.reservation.model.ReservationsModel;
import mg.reservation.panel.ReservationsPanel;
import mg.reservation.panel.WeekSelectPanel;
import mg.reservation.service.ReservationService;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class ReservationListPage extends WebPage {

	private static final long serialVersionUID = -7385489983766355838L;

	@Inject
	private ReservationService reservationService;

	private ReservationsPanel reservationsPanel;
	private WeekSelectPanel weekSelectPanel;

	private ReservationsModel reservationsModel;

	public ReservationListPage(final PageParameters parameters) {
		super(parameters);

		reservationsModel = new ReservationsModel(reservationService);

		WebMarkupContainer parent = new WebMarkupContainer("parent");
		parent.setOutputMarkupId(true);

		reservationsPanel = new ReservationsPanel("reservationsList", reservationsModel);
		weekSelectPanel = new WeekSelectPanel("weekSelect", reservationsModel);

		parent.add(reservationsPanel);
		parent.add(weekSelectPanel);

		add(parent);
	}
}
