package mg.wicketapp2.config;

import mg.wicketapp2.page.FormPage;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.odlabs.wiquery.ui.themes.WiQueryCoreThemeResourceReference;

public class WicketApplication extends WebApplication {

	@Override
	public Class<? extends WebPage> getHomePage() {
		return FormPage.class;
	}

	@Override
	public void init() {
		super.init();

		mountPage("/formpage", FormPage.class);

		addResourceReplacement(WiQueryCoreThemeResourceReference.get(),
				new WiQueryCoreThemeResourceReference("smoothness"));

		getMarkupSettings().setStripWicketTags(true);
	}
}
