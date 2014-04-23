package mg.tracking.event.tracker.common;

import mg.tracking.event.Event;
import mg.tracking.event.predicate.common.Predicate;

public class ToggleTracker extends BooleanTracker {

	private boolean continuousTracking = false;

	public ToggleTracker(Predicate predicate) {
		super(predicate);
	}

	@Override
	public void track(Event event1, Event event2) {

		// track continuously or toggle it up before this step (i.e. pick up the event)
		if (continuousTracking || (!continuousTracking && predicate.apply(event1, event2))) {

			quantity = quantity + calculateQuantity(event1, event2);

			// if we are currently tracking, disable the tracking after the event that toggles the tracking off
			if (predicate.apply(event1, event2)) {
				continuousTracking = !continuousTracking;
			}
		}
	}
}
