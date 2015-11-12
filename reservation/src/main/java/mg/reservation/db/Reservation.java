package mg.reservation.db;

import static mg.util.Common.yyyyMMddHHmmFormatter;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;
import static mg.util.validation.rule.ValidationRule.NOT_NULL_OR_EMPTY_STRING;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import mg.util.validation.Validator;
import mg.util.validation.rule.ValidationRule;

@XmlRootElement
public class Reservation implements Serializable {

	private static final long serialVersionUID = 8599865694082857673L;
	private String id = "";
	private String resource = "";
	private String reserver = "";
	private Date start = new Date();
	private Date end = new Date();
	private String description = "";
	private String title = "";
	
	// XXX: DB not yet altered for these:
	// private boolean allDay = false;
	// private boolean recurring = false;

	public Reservation() {
	}

	public Reservation(String id, String resource, String reserver, Date start, Date end, String title, String description) {

		new Validator()
				.add("resource", resource, NOT_NULL_OR_EMPTY_STRING)
				.add("reserver", reserver, NOT_NULL_OR_EMPTY_STRING)
				.add("start", start, NOT_NULL)
				.add("end", end, NOT_NULL)
				.add("title", title, NOT_NULL_OR_EMPTY_STRING)
				.validate();

		this.id = id;
		this.resource = resource;
		this.reserver = reserver;
		this.start = start;
		this.end = end;
		this.description = description;
		this.title = title;		
	}

	@Override
	public String toString() {
		String startingTime = yyyyMMddHHmmFormatter.format(start);
		String endingTime = yyyyMMddHHmmFormatter.format(end);
		// intentionally not all of the fields here.
		return String.format("(Id: %s, start time: %s, end time: %s)", id, startingTime, endingTime);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
