package mg.tracking.event.predicate;

import static mg.tracking.event.EventType.ATTACHING_TRAILER_END;
import static mg.tracking.event.EventType.DETACHING_TRAILER_END;
import static mg.tracking.event.EventType.LOADING_END;
import static mg.tracking.event.EventType.OFFLOADING_END;
import mg.tracking.event.Event;
import mg.tracking.event.EventType;
import mg.tracking.event.predicate.common.Delta;

public class OrderDependentDelta extends Delta {

	@Override
	public int apply(Event event1, Event event2) {

		// EventType event1Type = event1.getEventType();
		EventType event2Type = event2.getEventType();

		if (event2Type.equals(LOADING_END) ||
				event2Type.equals(ATTACHING_TRAILER_END)) {
			return 1;
		}

		if (event2Type.equals(OFFLOADING_END) ||
				event2Type.equals(DETACHING_TRAILER_END)) {
			return -1;
		}

		// default no change, no orders being removed or added.
		return 0;
	}
}
