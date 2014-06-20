package mg.wicketapp2.panel;

import java.util.Date;

import mg.wicketapp2.model.Info;
import mg.wicketapp2.validation.DateNotInFutureValidator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.odlabs.wiquery.ui.datepicker.DatePicker;

public class InfoPanel extends Panel {

	private static final long serialVersionUID = 5038748915975921172L;

	private Form<Info> form;
	private TextField<String> name;
	private TextField<String> email;
	private TextField<String> street;
	private TextField<String> zipCode;
	private TextField<String> town;

	private DatePicker<Date> datePicker;

	public InfoPanel(String id, CompoundPropertyModel<Info> infoModel) {
		super(id, infoModel);

		setDefaultModel(infoModel);

		form = new Form<Info>("info");
		name = new TextField<String>("name");
		name.setRequired(true);
		name.add(StringValidator.minimumLength(6)); // Aku Ii
		email = new TextField<String>("email");
		email.setRequired(true);
		email.add(EmailAddressValidator.getInstance());
		street = new TextField<String>("street");
		street.setRequired(true);
		street.add(StringValidator.minimumLength(4)); // katu
		zipCode = new TextField<String>("zipCode");
		zipCode.setRequired(true);
		zipCode.add(new PatternValidator("^\\d{5}$")); // 00420
		town = new TextField<String>("town");
		town.setRequired(true);
		town.add(StringValidator.minimumLength(2)); // Ii
		datePicker = new DatePicker<Date>("date");
		datePicker.setRequired(true);
		datePicker.add(new DateNotInFutureValidator());

		form.add(name);
		form.add(email);
		form.add(street);
		form.add(zipCode);
		form.add(town);
		form.add(datePicker);

		add(form);
	}

	public void setFieldsReadonly() {
		// just to show programmatic attribute appending by setting the fields as readonly:
		AttributeAppender readonly = AttributeModifier.append("readonly", "readonly");
		name.add(readonly);
		email.add(readonly);
		street.add(readonly);
		zipCode.add(readonly);
		town.add(readonly);
		datePicker.add(readonly);
	}
}
