package mg.tracking.parser;

import static java.lang.String.format;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mg.tracking.common.CommonUtil;
import mg.tracking.event.Event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class CsvParser {

	public static final String EVENTS_FILE_NAME = "events.csv";
	private static final Logger logger = LogManager.getLogger(CsvParser.class.getName());

	/**
	 * Reads the default EVENTS_FILE_NAME file in the startup directory and returns the events therein.
	 * 
	 * @throws IOException
	 *             If reading fails.
	 * @throws FileNotFoundException
	 *             If file name provided does not point to any file present.
	 */
	public List<Event> readFile() throws FileNotFoundException, IOException {
		return readFile(EVENTS_FILE_NAME);
	}

	public List<Event> readFile(String fileName) throws IOException, FileNotFoundException {

		CSVReader csvReader = null;
		List<Event> events = new ArrayList<Event>();

		try {
			csvReader = new CSVReader(new FileReader(fileName));

			List<String[]> allLines = csvReader.readAll();

			for (String[] sa : allLines) {
				events.add(new Event(sa));
			}

			logger.info(format("Read events list, size: %s.", events.size()));
			for (Event e : events) {
				logger.info(e.toString());
			}

		} finally {
			CommonUtil.close(csvReader);
		}

		return events;
	}

}
