package mg.tracking.event.tracker;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import mg.tracking.event.Event;
import mg.tracking.event.EventTracker;
import mg.tracking.event.EventType;
import mg.tracking.event.predicate.WorkShiftDurationTogglePredicate;
import mg.tracking.event.tracker.common.Tracker;

import org.joda.time.DateTime;
import org.junit.Test;

/*
 * Note that the test coverage is low for a reason: a pair coding session for 
 * creating tests would be preferable. This way both parties would get a sample
 * of the others teamworking skills.
 * 
 * A test class is provided for introductory example purposes.
 */
public class MinsBetweenToggleTrackerTest {

	private static final int ORDER_ID_1 = 1;
	private static final boolean IS_NOT_TRAILER = false;
	private static final String VEHICLE_1 = "Vehicle1";
	private static final String EMPLOYEE_A = "EmployeeA";
	private EventTracker eventTracker = new EventTracker();

	WorkShiftDurationTogglePredicate workShiftDurationPredicate = new WorkShiftDurationTogglePredicate();

	@Test
	public void shouldGet180minsSimpleTestNoEventsInBetween() {

		MinsBetweenToggleTracker minsBetweenTracker = getNewMinsBetweenTrackerForWorkShiftDurationPredicate();

		int meterAtStart = 0;
		int meterAtEnd = 100;

		Event workShiftStartEvent = makeEvent(getTimeNow(), EMPLOYEE_A, VEHICLE_1, meterAtStart, EventType.SHIFT_START, IS_NOT_TRAILER, ORDER_ID_1);
		Event workShiftEndEvent = makeEvent(getTimeWithOffSet(180), EMPLOYEE_A, VEHICLE_1, meterAtEnd, EventType.SHIFT_END, IS_NOT_TRAILER, ORDER_ID_1);

		minsBetweenTracker.track(workShiftStartEvent, workShiftEndEvent);

		int quantity = minsBetweenTracker.getQuantity();

		assertEquals("Workshift duration should be 180 (mins).", 180, quantity);
	}

	@Test
	public void shouldGet180minsWithOtherEvents() {
				
		List<Event> events = new ArrayList<Event>();
		List<Tracker> trackers = new ArrayList<Tracker>();

		MinsBetweenToggleTracker minsBetweenWorkshiftTracker = getNewMinsBetweenTrackerForWorkShiftDurationPredicate();
		trackers.add(minsBetweenWorkshiftTracker);

		int meter = 0;

		events.add(makeEvent(getTimeNow(), EMPLOYEE_A, VEHICLE_1, meter, EventType.SHIFT_START, IS_NOT_TRAILER, ORDER_ID_1));
		events.add(makeEvent(getTimeWithOffSet(60), EMPLOYEE_A, VEHICLE_1, meter, EventType.BREAK_START, IS_NOT_TRAILER, ORDER_ID_1));
		events.add(makeEvent(getTimeWithOffSet(120), EMPLOYEE_A, VEHICLE_1, meter, EventType.BREAK_END, IS_NOT_TRAILER, ORDER_ID_1));
		events.add(makeEvent(getTimeWithOffSet(180), EMPLOYEE_A, VEHICLE_1, meter + 100, EventType.SHIFT_END, IS_NOT_TRAILER, ORDER_ID_1));

		eventTracker.trackEvents(trackers, events);

		int quantity = minsBetweenWorkshiftTracker.getQuantity();

		assertEquals("Workshift duration should be 180 (mins) while traversing over other events.", 180, quantity);
	}

	private MinsBetweenToggleTracker getNewMinsBetweenTrackerForWorkShiftDurationPredicate() {
		return new MinsBetweenToggleTracker(workShiftDurationPredicate, "");
	}

	private DateTime getTimeWithOffSet(int offSetMinutes) {
		return new DateTime().plusMinutes(offSetMinutes);
	}

	private Event makeEvent(DateTime time, String employeeId, String vehicleId, int meter, EventType eventType, boolean isTrailer, int orderId) {
		return new Event(time, employeeId, vehicleId, meter, eventType, isTrailer, orderId);
	}

	private DateTime getTimeNow() {
		return new DateTime();
	}

}
