package mg.wicketapp2.page;

import mg.wicketapp2.panel.mainpage.FooterPanel;
import mg.wicketapp2.panel.mainpage.HeaderPanel;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.odlabs.wiquery.core.events.Event;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryEventBehavior;
import org.odlabs.wiquery.core.javascript.JsScope;
import org.odlabs.wiquery.ui.dialog.Dialog;

public abstract class MainPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public MainPage(final PageParameters parameters) {
		super(parameters);

		HeaderPanel headerPanel = new HeaderPanel("header");
		FooterPanel footerPanel = new FooterPanel("footer");
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");

		add(headerPanel);
		add(footerPanel);
		add(feedbackPanel);

		// TODO: remove example dialog usage
		final Dialog dialog = new Dialog("dialog");
		add(dialog);

		Button button = new Button("open-dialog");
		button.add(new WiQueryEventBehavior(new Event(MouseEvent.CLICK) {

			private static final long serialVersionUID = 1L;

			@Override
			public JsScope callback() {
				return JsScope.quickScope(dialog.open().render());
			}

		}));
		add(button);

	}

	public MainPage() {
	}
}
