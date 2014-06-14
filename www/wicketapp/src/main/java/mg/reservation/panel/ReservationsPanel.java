package mg.reservation.panel;

import java.util.List;

import mg.reservation.db.Reservation;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

public class ReservationsPanel extends Panel {

	private static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";

	public ReservationsPanel(String id, List<Reservation> reservations) {
		super(id);

		add(new ListView<Reservation>("reservations", reservations) {

			private static final long serialVersionUID = -8328953506175828323L;

			@Override
			protected void populateItem(ListItem<Reservation> item) {

				CompoundPropertyModel<Reservation> model = new CompoundPropertyModel<Reservation>(item.getModel());
				item.setModel(model);
				item.add(new Label("id"));
				item.add(new Label("title"));
				item.add(new DateLabel("start", new PatternDateConverter(DD_MM_YYYY_HH_MM, false)));
				item.add(new DateLabel("end", new PatternDateConverter(DD_MM_YYYY_HH_MM, false)));
			}

		});
	}

	private static final long serialVersionUID = 830442203196048049L;

}
