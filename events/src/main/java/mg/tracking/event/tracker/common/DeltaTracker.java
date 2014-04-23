package mg.tracking.event.tracker.common;

import mg.tracking.event.Event;
import mg.tracking.event.predicate.common.Delta;

public abstract class DeltaTracker extends TrackerUtil implements Tracker {

	protected int quantity = 0;
	protected Delta predicate = null;

	public DeltaTracker(Delta predicate) {
		this.predicate = predicate;
	}

	/**
	 * Calculates and adds to quantity only if the predicate provided is satisfied (a greater than zero is returned).
	 */
	public void track(Event event1, Event event2) {

		if (predicate.apply(event1, event2) > 0) {
			quantity = quantity + calculateQuantity(event1, event2);
		}
	}

	/**
	 * Override for quantity calculation. I.e kms or minutes.
	 */
	public int calculateQuantity(Event event1, Event event2) {
		return 0;
	}

	public int getQuantity() {
		return quantity;
	}

	/**
	 * Override for reporting. I.e. 'Total x h y mins.'
	 */
	public String getReport() {
		return "";
	}
}