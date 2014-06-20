package mg.wicketapp2.page;

import mg.wicketapp2.panel.mainpage.FooterPanel;
import mg.wicketapp2.panel.mainpage.HeaderPanel;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
	}

	public MainPage() {
	}
}
