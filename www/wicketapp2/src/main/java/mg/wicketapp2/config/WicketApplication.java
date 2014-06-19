package mg.wicketapp2.config;

import mg.wicketapp2.page.MainPage;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.odlabs.wiquery.ui.themes.WiQueryCoreThemeResourceReference;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see mg.wicketapp2.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage() {
		return MainPage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init() {
		super.init();
		
		addResourceReplacement(WiQueryCoreThemeResourceReference.get(), 
				new WiQueryCoreThemeResourceReference("smoothness"));
	}
}
