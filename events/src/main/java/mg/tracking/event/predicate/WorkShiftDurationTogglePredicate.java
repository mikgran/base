package mg.tracking.event.predicate;

import mg.tracking.event.Event;
import mg.tracking.event.EventType;
import mg.tracking.event.predicate.common.Predicate;

public class WorkShiftDurationTogglePredicate extends Predicate {

	@Override
	public boolean apply(Event event1, Event event2) {

		// if work shift starts or ends -> toggle
		// toimprove: validate event order and duplicates		
		if (event1.getEventType().equals(EventType.SHIFT_START) ||
				event2.getEventType().equals(EventType.SHIFT_END)) {
			return true;
		}

		return false;
	}

}


