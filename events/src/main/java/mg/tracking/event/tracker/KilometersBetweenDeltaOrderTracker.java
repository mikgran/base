package mg.tracking.event.tracker;

import mg.tracking.event.Event;
import mg.tracking.event.predicate.common.Delta;
import mg.tracking.event.tracker.common.OrderDependentDeltaTracker;

public class KilometersBetweenDeltaOrderTracker extends OrderDependentDeltaTracker {

	private String reportMessage = "";
	
	public KilometersBetweenDeltaOrderTracker(Delta predicate, String reportMessage) {
		super(predicate);
		this.reportMessage = reportMessage;
	}

	@Override
	public int calculateQuantity(Event event1, Event event2) {
		return kilometersBetween(event1, event2);
	}

	@Override
	public String getReport() {
		return String.format(reportMessage, quantity); 
	}
}
