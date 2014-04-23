package mg.tracking.event.predicate;

import mg.tracking.event.Event;
import mg.tracking.event.EventType;
import mg.tracking.event.predicate.common.Predicate;
import static mg.tracking.event.EventType.*;

public class BillableDurationPredicate extends Predicate {

	@Override
	public boolean apply(Event event1, Event event2) {

		EventType event1Type = event1.getEventType();
		EventType event2Type = event2.getEventType();

		// toimprove: add validation for the event order and allowed state transitions?
		if (event1Type.equals(LOADING_START) &&
				event2Type.equals(LOADING_END) ||

				event1Type.equals(OFFLOADING_START) &&
				event2Type.equals(OFFLOADING_END) ||

				event1Type.equals(LOADING_START) &&
				event2Type.equals(BREAK_START) ||

				event1Type.equals(OFFLOADING_START) &&
				event2Type.equals(BREAK_START) ||

				event1Type.equals(BREAK_END) &&
				event2Type.equals(OFFLOADING_END)) {

			return true;
		}

		return false;
	}
}
