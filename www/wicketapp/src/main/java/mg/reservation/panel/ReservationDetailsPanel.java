package mg.reservation.panel;

import mg.reservation.db.Reservation;
import mg.reservation.page.ReservationDetailsPage;
import mg.reservation.util.Common;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationDetailsPanel extends Panel {

	private static final long serialVersionUID = 5038748915975921172L;
	private Logger logger = LoggerFactory.getLogger(ReservationDetailsPage.class);
	private TextField<String> idLabel;
	private TextField<String> titleLabel;
	private DateTextField startDateTextField;
	private DateTextField endDateTextField;
	private boolean isEditingNewReservation = false;
	private Form<Reservation> form;

	public ReservationDetailsPanel(String id, IModel<Reservation> model) {
		super(id, new CompoundPropertyModel<Reservation>(model));

		logger.debug("init()");

		initializeInfoFields();
	}

	public ReservationDetailsPanel(String id) {
		super(id, new CompoundPropertyModel<Reservation>(new Reservation()));

		logger.debug("init() editing new reservation.");
		isEditingNewReservation = true;

		initializeInfoFields();
	}

	private void initializeInfoFields() {

		idLabel = new TextField<String>("id");
		titleLabel = new TextField<String>("title");
		startDateTextField = getDateTextFieldFor("start");
		endDateTextField = getDateTextFieldFor("end");

		form = getDetailsContentForm();

		form.add(idLabel);
		form.add(titleLabel);
		form.add(startDateTextField);
		form.add(endDateTextField);

		add(form);
	}

	private DateTextField getDateTextFieldFor(String id) {
		return new DateTextField(id, new PatternDateConverter(Common.DD_MM_YYYY_HH_MM, false));
	}

	private Form<Reservation> getDetailsContentForm() {
		return new Form<Reservation>("detailsContent") {

			private static final long serialVersionUID = -4067931937311221L;

			@Override
			protected void onSubmit() {

				logger.debug("detailsContentForm onSubmit()");

				if (isEditingNewReservation) {

				}
			}
		};
	}
}
