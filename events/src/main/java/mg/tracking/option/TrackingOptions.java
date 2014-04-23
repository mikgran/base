package mg.tracking.option;

import mg.tracking.event.ReportType;
import mg.tracking.parser.Parsers;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.joda.time.DateTime;

@SuppressWarnings("static-access")
public class TrackingOptions {

	private static final int NUMBER_OF_W_ARGUMENTS = 5;
	private static final int NUMBER_OF_V_ARGUMENTS = 5;

	private CommandLineParser parser = new BasicParser();
	private Options options = new Options();
	private ReportType reportType = ReportType.NONE;

	private String workerId = "";
	private String vehicleId = "";
	private DateTime workerStartDateTime = new DateTime();
	private DateTime workerEndDateTime = new DateTime();
	private DateTime vehicleStartDateTime = new DateTime();
	private DateTime vehicleEndDateTime = new DateTime();

	private final HelpFormatter helpFormatter = new HelpFormatter();

	public TrackingOptions(String[] args) throws ParseException {

		createCommandLineOptions(options);

		handleOptions(options, args);
	}

	public DateTime getVehicleDateTimeEnd() {
		return vehicleEndDateTime;
	}

	public DateTime getVehicleDateTimeStart() {
		return vehicleStartDateTime;
	}

	public String getWorkerId() {
		return workerId;
	}

	public DateTime getWorkerDateTimeStart() {
		return workerStartDateTime;
	}

	public DateTime getWorkerDateTimeEnd() {
		return workerEndDateTime;
	}

	public ReportType getReportType() {
		return reportType;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	private void createCommandLineOptions(Options opts) {

		Option workerreport = OptionBuilder.withArgName("workerreport")
				.hasArgs(NUMBER_OF_W_ARGUMENTS)
				.withDescription("The hours report for an employee.\n"
						+ "-w [start date time] [end date time] [worker id]\n"
						+ "-w 01.01.2013 00:00 02.01.2013 00:00 A")
				.create("w");

		Option vehicleReport = OptionBuilder.withArgName("vehiclereport")
				.hasArgs(NUMBER_OF_V_ARGUMENTS)
				.withDescription("The kilometers report for a vehicle.\n"
						+ "-v [start date time] [end date time] [vehicle id]\n"
						+ "-v 01.01.2013 00:00 02.01.2013 00:00 1")
				.create("v");

		opts.addOption(workerreport);
		opts.addOption(vehicleReport);
	}

	private void handleOptions(Options opts, String[] args) throws ParseException {

		if (args.length == 0) {
			printHelp();
			return;
		}

		CommandLine commandLine = parser.parse(options, args);

		if (commandLine.hasOption("w")) {
			handleOptionW(commandLine);
		}

		if (commandLine.hasOption("v")) {
			handleOptionV(commandLine);
		}
	}

	private String[] parseOptionValues(String optionKey, int numberOfArguments, CommandLine commandLine) {

		String[] optionValues = commandLine.getOptionValues(optionKey);

		if (optionValues.length != numberOfArguments) {
			System.out.println("Invalid argumens for: " + optionKey);
			printHelp();
			return null;
		}

		return optionValues;
	}

	private void handleOptionW(CommandLine commandLine) {

		String[] optionValues = parseOptionValues("w", NUMBER_OF_W_ARGUMENTS, commandLine);

		if (optionValues == null) {
			return;
		}

		// toimprove: add validation
		workerStartDateTime = parseStart(optionValues);
		workerEndDateTime = parseEnd(optionValues);
		workerId = optionValues[4];
		reportType = ReportType.WORKER;

		System.out.printf("%s, %s, %s", workerStartDateTime.toString("dd.MM.yyyy HH:mm"), workerEndDateTime.toString("dd.MM.yyyy HH:mm"), workerId);

	}

	private void handleOptionV(CommandLine commandLine) {

		String[] optionValues = parseOptionValues("v", NUMBER_OF_V_ARGUMENTS, commandLine);

		if (optionValues == null) {
			return;
		}

		// toimprove: add validation
		vehicleStartDateTime = parseStart(optionValues);
		vehicleEndDateTime = parseEnd(optionValues);
		vehicleId = optionValues[4];
		reportType = ReportType.VEHICLE;

		System.out.printf("%s, %s, %s", workerStartDateTime.toString("dd.MM.yyyy HH:mm"), workerEndDateTime.toString("dd.MM.yyyy HH:mm"), vehicleId);
	}

	// toimprove: add validation for these
	private DateTime parseEnd(String[] optionValues) {
		return Parsers.formatterddMMyyyyHHmm.parseDateTime(optionValues[2] + " " + optionValues[3]);
	}

	private DateTime parseStart(String[] optionValues) {
		return Parsers.formatterddMMyyyyHHmm.parseDateTime(optionValues[0] + " " + optionValues[1]);
	}

	private void printHelp() {
		helpFormatter.printHelp("Report", options);
	}

}
