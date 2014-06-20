package mg.wicketapp2.page;

import java.util.Locale;

import mg.wicketapp2.model.Info;
import mg.wicketapp2.panel.InfoPanel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class FormPage extends MainPage {

	private static final long serialVersionUID = 5905670521541567352L;
	private CompoundPropertyModel<Info> infoModel;

	public FormPage(PageParameters parameters) {
		super(parameters);

		changeLocale();

		infoModel = new CompoundPropertyModel<Info>(new Info());

		Form<Info> form = new Form<Info>("form");
		form.add(new InfoPanel("info", infoModel));
		form.add(new Button("submit") {
			private static final long serialVersionUID = -921752319767345120L;

			@Override
			public void onSubmit() {
				setResponsePage(new ResultPage(new PageParameters(), infoModel));
			}

		});

		add(new Label("title", new Model<String>("Information")));
		add(form);
	}

	public void changeLocale() {
		// This here redirects to a gathering page and collects the navigatorLanguage=fi setting among others.
		// It also causes tests to funk up since there is an extra page transition.

		getApplication().getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
		WebClientInfo webClientInfo = (WebClientInfo) getSession().getClientInfo();
		ClientProperties properties = webClientInfo.getProperties();
		System.out.println(properties.toString());

		if (!properties.getNavigatorLanguage().contains("en")) {
			getSession().setLocale(new Locale(properties.getNavigatorLanguage()));
		}
	}

}
