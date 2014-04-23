package mg.tracking.event.tracker;

import mg.tracking.event.Event;
import mg.tracking.event.predicate.common.Delta;
import mg.tracking.event.tracker.common.NoOrderDeltaTracker;

public class KilometersBetweenDeltaNoOrderTracker extends NoOrderDeltaTracker {

	private String reportMessage = "";
	
	public KilometersBetweenDeltaNoOrderTracker(Delta predicate, String reportMessage) {
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
