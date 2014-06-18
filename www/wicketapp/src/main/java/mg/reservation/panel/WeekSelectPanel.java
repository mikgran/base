package mg.reservation.panel;

import mg.reservation.model.ReservationsModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekSelectPanel extends Panel {

	private Logger logger = LoggerFactory.getLogger(WeekSelectPanel.class);
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
				logger.debug("AjaxSubmitLink onSubmit() Refreshing parent");
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
				String week = weekField.getModelObject();
				reservationsModel.setSelectedWeek(week);
				logger.debug("Form<ReservationsModel> onSubmit() week: {}", week);
			}
		};
	}

}
