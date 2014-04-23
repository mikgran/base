package mg.tracking.event.tracker.common;

import java.util.HashSet;
import java.util.Set;

import mg.tracking.event.Event;
import mg.tracking.event.predicate.common.Delta;

public class OrderDependentDeltaTracker extends DeltaTracker {

	private Set<Integer> orderIds = new HashSet<Integer>();

	public OrderDependentDeltaTracker(Delta predicate) {
		super(predicate);
	}

	@Override
	public void track(Event event1, Event event2) {

		int delta = predicate.apply(event1, event2);

		if (delta > 0) {

			if (event2.getOrderId() != 0) {
				orderIds.add(event2.getOrderId());
			}

		} else if (delta < 0) {

			if (event2.getOrderId() != 0) {
				orderIds.remove(event2.getOrderId());
			}

		}
		
		// if there is at least one orderId lingering around, we add the quantity
		if (orderIds.size() > 0) {
			
			quantity = quantity + calculateQuantity(event1, event2);
		}
	}
}
