package mg.reservation.panel;

import mg.reservation.model.ReservationsModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekSelectPanel extends Panel {

	private static final long serialVersionUID = 612552405494581062L;
	private Logger logger = LoggerFactory.getLogger(WeekSelectPanel.class);
	private TextField<String> weekField;
	private WebMarkupContainer parent;
	private ReservationsModel reservationsModel;

	public WeekSelectPanel(String id, ReservationsModel reservationsModel, final WebMarkupContainer parent) {
		super(id);
		this.reservationsModel = reservationsModel;
		this.parent = parent;

		weekField = new TextField<String>("week", Model.of(""));

		Form form = getWeekSelectionForm();
		form.add(new Label("currentWeek", new Model<String>(reservationsModel.getSelectedWeekAsString())));
		form.add(weekField);

		form.add(new AjaxSubmitLink("ajaxSubmit") { // TODO generics
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				logger.info("Adding parent to be refreshed.");
				target.add(parent);
			}
		});

		add(form);
	}

	// TODO generics
	private Form getWeekSelectionForm() {
		return new Form("weekSelection") {
			private static final long serialVersionUID = -3756364422045777230L;

			@Override
			protected void onSubmit() {
				logger.info("form was submitted with {}", weekField.getModelObject()); // TODO remove
				reservationsModel.setWeek(weekField.getModelObject());
			}
		};
	}

}
