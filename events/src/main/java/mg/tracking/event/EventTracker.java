package mg.tracking.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mg.tracking.event.predicate.BillableDurationPredicate;
import mg.tracking.event.predicate.BreakDurationPredicate;
import mg.tracking.event.predicate.OrderDependentDelta;
import mg.tracking.event.predicate.WorkShiftDurationTogglePredicate;
import mg.tracking.event.tracker.KilometersBetweenDeltaNoOrderTracker;
import mg.tracking.event.tracker.KilometersBetweenDeltaOrderTracker;
import mg.tracking.event.tracker.MinsBetweenToggleTracker;
import mg.tracking.event.tracker.MinsBetweenTracker;
import mg.tracking.event.tracker.common.BooleanTracker;
import mg.tracking.event.tracker.common.DeltaTracker;
import mg.tracking.event.tracker.common.Tracker;
import mg.tracking.option.TrackingOptions;
import mg.tracking.parser.CsvParser;
import mg.tracking.parser.Parsers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class EventTracker {
	
	private static final Logger logger = LogManager.getLogger(EventTracker.class);
	private CsvParser csvParser = new CsvParser();

	public void trackAndReport(TrackingOptions options) throws IllegalArgumentException, FileNotFoundException, IOException {

		List<Event> events = csvParser.readFile();
		List<Tracker> trackers = new ArrayList<Tracker>();
		String fileName = "";

		if (options.getReportType().equals(ReportType.WORKER)) {

			BooleanTracker workShiftToggleTracker = new MinsBetweenToggleTracker(new WorkShiftDurationTogglePredicate(), "total working duration: %s.");
			BooleanTracker breakDurationTracker = new MinsBetweenTracker(new BreakDurationPredicate(), "total break duration: %s.");
			BooleanTracker billableDurationTracker = new MinsBetweenTracker(new BillableDurationPredicate(), "total billable duration: %s.");

			trackers.add(workShiftToggleTracker);
			trackers.add(breakDurationTracker);
			trackers.add(billableDurationTracker);

			calculateWorkerTotals(options, events, trackers);

			fileName = String.format("totals-worker%s-start%s-end%s-at%s.txt", options.getWorkerId(),
					options.getWorkerDateTimeStart().toString("ddMMyyyyHHmm"),
					options.getWorkerDateTimeEnd().toString("ddMMyyyyHHmm"),
					Parsers.formatterddMMyyyyHHmmssTight.print(new DateTime()));
		}

		if (options.getReportType().equals(ReportType.VEHICLE)) {

			DeltaTracker orderDependentTracker = new KilometersBetweenDeltaOrderTracker(new OrderDependentDelta(), "total kilometers with orders: %s.");
			DeltaTracker noOrdersTracker = new KilometersBetweenDeltaNoOrderTracker(new OrderDependentDelta(), "total kilometers without orders: %s.");

			trackers.add(orderDependentTracker);
			trackers.add(noOrdersTracker);

			calculateVehicleTotals(options, events, trackers);

			fileName = String.format("totals-vehicle%s-start%s-end%s-at%s.txt", options.getVehicleId(),
					options.getVehicleDateTimeStart().toString("ddMMyyyyHHmm"),
					options.getVehicleDateTimeEnd().toString("ddMMyyyyHHmm"),
					Parsers.formatterddMMyyyyHHmmssTight.print(new DateTime()));
		}

		if (trackers.size() > 0) {

			logger.info("Totals:");
			for (Tracker tracker : trackers) {
				logger.info(tracker.getReport());
			}

			ReportWriter reportWriter = new ReportWriter(fileName);

			reportWriter.write(trackers);
		}
	}

	private void calculateVehicleTotals(TrackingOptions options, List<Event> events, List<Tracker> trackers) {

		List<Event> vehicleEvents = filterByVehicleIdAndDates(events, options);

		logger.info("Filtered vehicle events:");
		for (Event event : vehicleEvents) {
			logger.info(event);
		}

		trackEvents(trackers, vehicleEvents);
	}

	private List<Event> filterByVehicleIdAndDates(List<Event> events, TrackingOptions options) {

		List<Event> filteredEvents = new ArrayList<Event>();
		for (Event event : events) {
			if (event.getVehicleId().equals(options.getVehicleId()) && isEventBetweenDates(event,
					options.getVehicleDateTimeStart(),
					options.getVehicleDateTimeEnd())) {

				filteredEvents.add(event);
			}
		}

		return filteredEvents;
	}

	private void calculateWorkerTotals(TrackingOptions options, List<Event> events, List<Tracker> trackers) {

		List<Event> workersEvents = filterByWorkerIdAndDates(events, options);

		logger.info("Filtered worker events:");
		for (Event event : workersEvents) {
			logger.info(event);
		}
		
		trackEvents(trackers, workersEvents);
	}

	private List<Event> filterByWorkerIdAndDates(List<Event> events, TrackingOptions options) {

		List<Event> filteredEvents = new ArrayList<Event>();

		for (Event event : events) {
			if (event.getWorkerId().equals(options.getWorkerId()) && isEventBetweenDates(event,
					options.getWorkerDateTimeStart(),
					options.getWorkerDateTimeEnd())) {

				filteredEvents.add(event);
			}
		}

		return filteredEvents;
	}

	private boolean isEventBetweenDates(Event event, DateTime start, DateTime end) {

		return (event.getEventTime().isEqual(start) ||
				event.getEventTime().isAfter(start)) &&

				(event.getEventTime().isEqual(end) ||
				event.getEventTime().isBefore(end));
	}

	/**
	 * Tracks the events by handling them in increments of twos. Every event pair is run 
	 * through a list of trackers. A minimum of two events is expected for the tracking to 
	 * succeed.
	 * 
	 * @param trackers
	 *            The services that perform the tracking and contain the results as quantity.
	 * @param events
	 *            the parsed list of events that will be tracked.
	 */
	public void trackEvents(List<Tracker> trackers, List<Event> events) throws IllegalArgumentException {

		if (events.isEmpty() || events.size() < 2) {
			throw new IllegalArgumentException("Not enough events to track with or wrong parameters. Expecting at least 2 events.");
		}

		Iterator<Event> eventIterator = events.iterator();

		Event event1 = eventIterator.next();
		Event event2 = eventIterator.next();

		runTrackers(trackers, event1, event2);

		while (eventIterator.hasNext()) {

			event1 = event2;
			event2 = eventIterator.next();

			runTrackers(trackers, event1, event2);
		}
	}

	private void runTrackers(List<Tracker> trackers, Event event1, Event event2) {
		for (Tracker tracker : trackers) {
			tracker.track(event1, event2);
		}
	}
}
