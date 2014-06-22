package mg.reservation.panel;

import mg.reservation.model.ReservationsModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.RangeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekSelectPanel extends Panel {

	private Logger logger = LoggerFactory.getLogger(WeekSelectPanel.class);
	private static final long serialVersionUID = 612552405494581062L;
	private NumberTextField<Integer> weekField;
	private ReservationsModel reservationsModel;
	private Form<ReservationsModel> form;

	public WeekSelectPanel(String id, ReservationsModel reservationsModel) {
		super(id);
		this.reservationsModel = reservationsModel;

		weekField = getWeekField();
		form = getWeekSelectionForm();
		form.add(weekField);
		form.add(getSetWeekAjaxSubmitLink());

		add(form);
	}

	private NumberTextField<Integer> getWeekField() {
		NumberTextField<Integer> weekField = new NumberTextField<Integer>("week", new Model<Integer>(), Integer.class);
		weekField.setRequired(true);
		weekField.setLabel(new Model<String>("weekLabel"));
		weekField.add(new RangeValidator<Integer>(1, 52));
		return weekField;
	}

	private AjaxSubmitLink getSetWeekAjaxSubmitLink() {
		return new AjaxSubmitLink("ajaxSetWeek") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				logger.debug("AjaxSubmitLink ajaxSetWeek onSubmit()");
				target.add(getParent());
			}
		};
	}

	private Form<ReservationsModel> getWeekSelectionForm() {

		Form<ReservationsModel> form = new Form<ReservationsModel>("weekSelection") {
			private static final long serialVersionUID = -3756364422045777230L;

			@Override
			protected void onSubmit() {
				Integer week = weekField.getModelObject();
				reservationsModel.setSelectedWeek(week);

				logger.debug("Form<ReservationsModel> weekSelection onSubmit() week: {}", week);
			}
		};

		form.add(new Label("currentWeek", new Model<String>(reservationsModel.getSelectedWeekAsString())));
		form.add(new FeedbackPanel("weekfeedback"));

		return form;
	}

}
