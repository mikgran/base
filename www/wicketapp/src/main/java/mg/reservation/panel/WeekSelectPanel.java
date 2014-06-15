package mg.reservation.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;

public class WeekSelectPanel extends Panel {

	private TextField<String> weekField;

	public WeekSelectPanel(String id) {
		super(id);

		weekField = new TextField<String>("week", Model.of(""));

		Form form = getWeekSelectionForm();
		form.add(new Label("currentWeek", new Model<String>(getCurrentWeekNumber())));
		form.add(weekField);

		add(form);
	}

	// TODO model type generics
	private Form getWeekSelectionForm() {
		return new Form("weekSelection") {
			private static final long serialVersionUID = -3756364422045777230L;

			@Override
			protected void onSubmit() {
				System.out.println("form was submitted with " + weekField.getModelObject());
			}
		};
	}

	private static final long serialVersionUID = 612552405494581062L;

	private String getCurrentWeekNumber() {
		return "" + new DateTime().weekOfWeekyear().get();
	}

}
