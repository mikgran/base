package mg.wicketapp2.page;

import mg.wicketapp2.model.Info;
import mg.wicketapp2.panel.InfoPanel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ResultPage extends MainPage {

	private static final long serialVersionUID = -8707510647144992483L;

	public ResultPage(final PageParameters parameters, CompoundPropertyModel<Info> infoModel) {
		super(parameters);

		// re-use the InfoPanel here as readonly.
		InfoPanel infoPanel = new InfoPanel("info", infoModel);
		infoPanel.setFieldsReadonly();
		
		add(new Label("title", new Model<String>("Result")));
		add(infoPanel);
	}

}
