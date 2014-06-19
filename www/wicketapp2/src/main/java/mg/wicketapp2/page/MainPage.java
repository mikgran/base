package mg.wicketapp2.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.odlabs.wiquery.core.events.Event;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryEventBehavior;
import org.odlabs.wiquery.core.javascript.JsScope;
import org.odlabs.wiquery.ui.dialog.Dialog;

public class MainPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public MainPage(final PageParameters parameters) {
		super(parameters);

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
}
