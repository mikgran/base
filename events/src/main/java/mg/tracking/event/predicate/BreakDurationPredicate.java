package mg.tracking.event.predicate;

import mg.tracking.event.Event;
import mg.tracking.event.EventType;
import mg.tracking.event.predicate.common.Predicate;

public class BreakDurationPredicate extends Predicate {

	@Override
	public boolean apply(Event event1, Event event2) {
		
		// toimprove: add validation for broken logs
		if (event1.getEventType().equals(EventType.BREAK_START) &&
				event2.getEventType().equals(EventType.BREAK_END)) {
			return true;
		}
			
		return false;
	}
	
}
