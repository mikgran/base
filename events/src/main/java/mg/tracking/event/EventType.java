package mg.tracking.event;

public enum EventType {

	// for simplicity sakes, no umlauts
	// toimprove: add different charset conversions or understanding of them to the program
	LOADING_START("Lastauksen alku"),
	LOADING_END("Lastauksen loppu"),
	OFFLOADING_START("Purun alku"),
	OFFLOADING_END("Purun loppu"),
	ATTACHING_TRAILER_START("Peravaunun kiinnityksen alku"),
	ATTACHING_TRAILER_END("Peravaunun kiinnityksen loppu"),
	DETACHING_TRAILER_START("Peravaunun irroituksen alku"),
	DETACHING_TRAILER_END("Peravaunun irroituksen loppu"),
	SHIFT_START("Tyovuoron alku"),
	SHIFT_END("Tyovuoron loppu"),
	BREAK_START("Tauon alku"),
	BREAK_END("Tauon loppu"),
	NONE("invalid event");

	private EventType(String type) {
		this.type = type;
	}

	private final String type;

	public String getType() {
		return type;
	}

	public static EventType from(String text) {
		if (text != null) {
			for (EventType et : EventType.values()) {
				if (text.trim().equalsIgnoreCase(et.type)) {
					return et;
				}
			}
		}
		return NONE;
	}
}
