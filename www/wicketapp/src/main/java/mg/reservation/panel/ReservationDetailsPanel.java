package mg.reservation.panel;

import mg.reservation.db.Reservation;
import mg.reservation.util.Common;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class ReservationDetailsPanel extends Panel {
	
	private static final long serialVersionUID = 5038748915975921172L;

	public ReservationDetailsPanel(String id, IModel<Reservation> model) {
		super(id, new CompoundPropertyModel<Reservation>(model));

		add(new Label("id"));
		add(new Label("title"));
		add(new DateLabel("start", new PatternDateConverter(Common.DD_MM_YYYY_HH_MM, false)));
		add(new DateLabel("end", new PatternDateConverter(Common.DD_MM_YYYY_HH_MM, false)));
	}
}
