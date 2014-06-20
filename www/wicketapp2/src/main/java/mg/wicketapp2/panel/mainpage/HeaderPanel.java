package mg.wicketapp2.panel.mainpage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class HeaderPanel extends Panel {

	private static final long serialVersionUID = 6814317123271041772L;

	public HeaderPanel(String id) {
		super(id);

		// TODO fix localization
		add(new Label("title", new Model<String>("Application")));
	}

}
