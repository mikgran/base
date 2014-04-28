package mg.reservation.dao;

import static mg.reservation.util.Common.yyyyMMddHHmmFormatter;

import java.util.Date;

public class Reservation {

	private long id = -1;
	private String resource = "";
	private String reserver = "";
	private Date startTime = new Date();
	private Date endTime = new Date();
	private String description = "";

	public Reservation() {
	}

	public Reservation(long id, String resource, String reserver, Date startTime, Date endTime, String description) {
		this.id = id;
		this.resource = resource;
		this.reserver = reserver;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		String startingTime = yyyyMMddHHmmFormatter.format(startTime);
		String endingTime = yyyyMMddHHmmFormatter.format(endTime);
		// intentionally not all of the fields here.
		return String.format("(Id: %s, start time: %s, end time: %s)", id, startingTime, endingTime);
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getReserver() {
		return reserver;
	}

	public void setReserver(String reserver) {
		this.reserver = reserver;
	}

}
