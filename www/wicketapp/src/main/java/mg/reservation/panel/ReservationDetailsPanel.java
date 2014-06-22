package mg.reservation.panel;

import mg.reservation.db.Reservation;
import mg.reservation.util.Common;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class ReservationDetailsPanel extends Panel {

	private static final long serialVersionUID = 5038748915975921172L;
	private TextField<String> idLabel;
	private TextField<String> titleLabel;
	private DateTextField startDateLabel;
	private DateTextField endDateLabel;

	public ReservationDetailsPanel(String id, IModel<Reservation> model) {
		super(id, new CompoundPropertyModel<Reservation>(model));

		idLabel = getIdField();
		titleLabel = getTitleField();
		startDateLabel = getStartDateField();
		endDateLabel = getEndDateField();

		add(idLabel);
		add(titleLabel);
		add(startDateLabel);
		add(endDateLabel);
	}

	private DateTextField getEndDateField() {
		return new DateTextField("end", getPatternDateConverterForFinDateTime());
	}

	private DateTextField getStartDateField() {
		return new DateTextField("start", getPatternDateConverterForFinDateTime());
	}

	private TextField<String> getTitleField() {
		return new TextField<String>("title");
	}

	private TextField<String> getIdField() {
		return new TextField<String>("id");
	}

	private PatternDateConverter getPatternDateConverterForFinDateTime() {
		return new PatternDateConverter(Common.DD_MM_YYYY_HH_MM, false);
	}
}
