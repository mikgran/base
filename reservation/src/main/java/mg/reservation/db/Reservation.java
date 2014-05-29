package mg.reservation.db;

import static mg.reservation.util.Common.yyyyMMddHHmmFormatter;

import java.util.Date;

import mg.reservation.validation.Validator;
import static mg.reservation.validation.rule.ValidationRule.*;

public class Reservation {

	private String id = "";
	private String resource = "";
	private String reserver = "";
	private Date startTime = new Date();
	private Date endTime = new Date();
	private String description = "";
	private String title = "";

	public Reservation() {
	}

	public Reservation(String id, String resource, String reserver, Date startTime, Date endTime, String title, String description) {

		new Validator()
				.add("resource", resource, NOT_NULL_OR_EMPTY_STRING)
				.add("reserver", reserver, NOT_NULL_OR_EMPTY_STRING)
				.add("startTime", startTime, NOT_NULL)
				.add("endTime", endTime, NOT_NULL)
				.add("title", title, NOT_NULL_OR_EMPTY_STRING)
				.validate();

		this.id = id;
		this.resource = resource;
		this.reserver = reserver;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
		this.title = title;		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
