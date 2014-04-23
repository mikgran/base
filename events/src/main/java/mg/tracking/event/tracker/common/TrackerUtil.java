package mg.tracking.event.tracker.common;

import mg.tracking.event.Event;

import org.joda.time.Minutes;

public class TrackerUtil {
	
	public int minsBetween(Event event1, Event event2) {
		return Minutes.minutesBetween(event1.getEventTime(), event2.getEventTime()).getMinutes();
	}

	public int kilometersBetween(Event event1, Event event2) {
		// toimprove: add validation for properly increasing meters and corrupt logs.
		return event2.getMeter() - event1.getMeter();
	}
	
	public String toHourMinsString(int minutes) {
		return String.format("%sh %smin", ((int) minutes / 60), minutes % 60);
	}
}
