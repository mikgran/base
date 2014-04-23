package mg.tracking.event;

import static mg.tracking.parser.Parsers.formatterddMMyyyyHHmm;
import static mg.tracking.parser.Parsers.trimOrElse;
import static mg.tracking.parser.Parsers.parseIntOrElse;

import org.joda.time.DateTime;

public class Event {

	private DateTime eventTime = null;
	private String workerId = "";
	private String vehicleId = "";
	private int meter = 0;
	private EventType eventType = EventType.NONE;
	private boolean trailer = false;
	private int orderId = 0;
	
	public Event(DateTime eventTime,
			String employee,
			String vehicle,
			int meter,
			EventType eventType,
			boolean trailer,
			int orderId) {

		this.eventTime = eventTime;
		this.workerId = employee;
		this.vehicleId = vehicle;
		this.meter = meter;
		this.eventType = eventType;
		this.trailer = trailer;
		this.orderId = orderId;
	}

	public Event(String[] elements) {

		if (elements == null || elements.length < 6) {
			return;
		}

		// toimprove: validation, proper error handling
		int i = 0;
		this.eventTime = formatterddMMyyyyHHmm.parseDateTime(elements[i++]);
		this.workerId = trimOrElse(elements[i++], "");
		this.vehicleId = trimOrElse(elements[i++], "");
		this.meter = parseIntOrElse(elements[i++], 0);
		this.eventType = EventType.from(elements[i++]);
		this.trailer = Boolean.parseBoolean(elements[i++]);
		this.orderId = parseIntOrElse(elements[i++], 0);
	}

	public DateTime getEventTime() {
		return eventTime;
	}

	public String getWorkerId() {
		return workerId;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public int getMeter() {
		return meter;
	}

	public EventType getEventType() {
		return eventType;
	}

	public boolean isTrailer() {
		return trailer;
	}

	public int getOrderId() {
		return orderId;
	}

	public String toString() {
		return String.format("Event (%s, %s, %s, %s, %s, %s, %s)",
				eventTime.toString("dd.MM.yyyy HH:mm"),
				workerId,
				vehicleId,
				meter,
				eventType,
				trailer,
				orderId);
	}
}
