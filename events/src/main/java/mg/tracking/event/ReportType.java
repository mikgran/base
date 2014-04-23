package mg.tracking.event;

public enum ReportType {
	WORKER("w"),
	VEHICLE("v"),
	NONE("n"); // toimprove: add validation and flow control for missing type?

	private ReportType(String type) {
		this.type = type;
	}

	private final String type;
	
	public String getType() {
		return type;
	}
}
