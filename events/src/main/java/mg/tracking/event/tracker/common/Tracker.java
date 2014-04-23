package mg.tracking.event.tracker.common;

import mg.tracking.event.Event;

public interface Tracker {
	
	/**
	 * Tracks two elements at a time and their state transition.
	 * @param event1 first element to be tracked
	 * @param event2 second element to be tracked
	 */
	public void track(Event event1, Event event2);
	
	/**
	 * Provides the report of this tracker.
	 * @return A report of tracked quantity in human readable form.
	 */
	public String getReport();
}
