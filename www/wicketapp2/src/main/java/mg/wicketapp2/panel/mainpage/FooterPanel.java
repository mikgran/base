package mg.wicketapp2.panel.mainpage;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class FooterPanel extends Panel {

	private static final long serialVersionUID = 6415685301360629243L;

	public FooterPanel(String id) {
		super(id);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		String dateTimeNow = dateFormatter.format(new Date());

		add(new Label("datetime", new Model<String>(dateTimeNow)));
	}

}
