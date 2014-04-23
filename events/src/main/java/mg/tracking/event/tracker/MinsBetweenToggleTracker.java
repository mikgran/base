package mg.tracking.event.tracker;

import mg.tracking.event.Event;
import mg.tracking.event.predicate.common.Predicate;
import mg.tracking.event.tracker.common.ToggleTracker;

public class MinsBetweenToggleTracker extends ToggleTracker {

	private String reportMessage = "";

	public MinsBetweenToggleTracker(Predicate predicate, String reportMessage) {
		super(predicate);
		this.reportMessage = reportMessage;
	}

	@Override
	public int calculateQuantity(Event event1, Event event2) {
		// the beef!
		return minsBetween(event1, event2);
	}
	
	@Override
	public String getReport() {
		return String.format(reportMessage, toHourMinsString(quantity)); 
	}
}
