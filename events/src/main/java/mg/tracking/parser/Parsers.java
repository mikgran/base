package mg.tracking.parser;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Parsers {

	// breaking the usual camel case here for clarity sakes.
	public static final DateTimeFormatter formatterddMMyyyyHHmm = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
	public static final DateTimeFormatter formatterdMyyyyHHmm = DateTimeFormat.forPattern("d.M.yyyy HH:mm"); // not used
	public static final DateTimeFormatter formatterddMMyyyyHHmmssTight = DateTimeFormat.forPattern("ddMMyyyy-HH-mm-ss");

	public static int parseIntOrElse(String stringToParse, int orElseValue) {

		try {
			return Integer.parseInt(stringToParse);
		} catch (NumberFormatException ignored) {
			return orElseValue;
		}
	}

	public static String trimOrElse(String stringToGet, String orElseValue) {

		if (stringToGet == null || stringToGet.length() == 0) {
			return orElseValue;
		}

		return stringToGet.trim();
	}

}
