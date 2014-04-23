package mg.tracking.event;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mg.tracking.common.CommonUtil;
import mg.tracking.event.tracker.common.Tracker;

public class ReportWriter {

	private static final Logger logger = LogManager.getLogger(ReportWriter.class);
	private BufferedWriter reportWriter = null;
	private StringBuffer logBuffer = new StringBuffer();

	public ReportWriter(String fileName) throws IOException {

		if (fileName == null || fileName.length() == 0) {
			throw new FileNotFoundException(String.format("No file named: '%s'.", fileName));
		}
		
		reportWriter = new BufferedWriter(new FileWriter(new File(fileName)));
	}

	public void write(List<Tracker> trackers) {

		if (trackers.size() == 0) {
			return;
		}
		
		for (Tracker tracker : trackers) {
			logBuffer.append(tracker.getReport());
			logBuffer.append("\n");
		}

		try {

			reportWriter.write(logBuffer.toString());
			reportWriter.flush();

		} catch (Exception e) {
			logger.error("Unable to write the report log.", e);
			CommonUtil.close(reportWriter);
		}

	}

}
