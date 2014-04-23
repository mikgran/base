package mg.tracking.event.predicate.common;

import mg.tracking.event.Event;

public abstract class Delta {

	/**
	 * Much like predicate, the delta apply is used to decipher the truth state, but with a catch: <br />
	 * The value zero represents no change in truth condition. Negatives represent false and <br />
	 * positives respectively true. Used in conjunction with a Tracker to trigger calculation inside the <br />
	 * tracker.
	 */
	public abstract int apply(Event event1, Event event2);
}
