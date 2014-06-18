package mg.reservation.panel;

import mg.reservation.model.ReservationsModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class WeekSelectPanel extends Panel {

	private static final long serialVersionUID = 612552405494581062L;
	private TextField<String> weekField;
	private ReservationsModel reservationsModel;

	public WeekSelectPanel(String id, ReservationsModel reservationsModel) {
		super(id);
		this.reservationsModel = reservationsModel;

		weekField = new TextField<String>("week", Model.of(""));

		Form<ReservationsModel> form = getWeekSelectionForm();
		form.add(new Label("currentWeek", new Model<String>(reservationsModel.getSelectedWeekAsString())));
		form.add(weekField);

		form.add(new AjaxSubmitLink("ajaxSubmit") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.add(getParent());
			}
		});

		add(form);
	}

	private Form<ReservationsModel> getWeekSelectionForm() {
		return new Form<ReservationsModel>("weekSelection") {
			private static final long serialVersionUID = -3756364422045777230L;

			@Override
			protected void onSubmit() {
				reservationsModel.setWeek(weekField.getModelObject());
			}
		};
	}

}
