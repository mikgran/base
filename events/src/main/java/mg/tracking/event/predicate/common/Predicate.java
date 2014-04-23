package mg.tracking.event.predicate.common;

import mg.tracking.event.Event;

public abstract class Predicate {

	/**
	 * Represents a condition of two events in which something is true.<br />
	 * Used in conjunction with a Tracker to trigger calculation inside the <br />
	 * tracker.
	 */
	public abstract boolean apply(Event event1, Event event2);
}