package mg.tracking.event;

import java.io.FileNotFoundException;
import java.io.IOException;

import mg.tracking.option.TrackingOptions;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Report {

	static final Logger logger = LogManager.getLogger(Report.class);

	public static void main(String[] args) {

		TrackingOptions options = null;
		EventTracker eventTracker = new EventTracker();

		try {

			options = new TrackingOptions(args);

			eventTracker.trackAndReport(options);

		} catch (ParseException e) {

			logger.error("Error parsing the events csv file.", e);

		} catch (IllegalArgumentException e) {

			logger.warn("Invalid arguments.", e);

		} catch (FileNotFoundException e) {

			logger.error("Unable to find a file.", e);

		} catch (IOException e) {

			logger.error("IO exception.", e);
		}
	}
}
