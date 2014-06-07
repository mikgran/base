package mg.reservation.rest;

import static mg.reservation.util.Common.getDateFrom;
import static mg.reservation.validation.rule.ValidationRule.NOT_NEGATIVE_OR_ZERO_AS_STRING;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import mg.reservation.db.DBConfig;
import mg.reservation.db.Reservation;
import mg.reservation.service.ReservationService;
import mg.reservation.util.Config;
import mg.reservation.validation.RestRequestParameterValidator;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/reservations")
public class ReservationResource {

	private Logger logger = LoggerFactory.getLogger(ReservationResource.class);
	private ReservationService reservationService = null;

	public ReservationResource() throws IOException { // allowing the missing configuration to fall back to OS
		reservationService = new ReservationService(new DBConfig(new Config()));

		PropertyConfigurator.configure("log4j.properties");
	}

	/**
	 * Exposed only for testing purposes.
	 * @param reservationService the service to use with the resource.
	 */
	protected ReservationResource(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	/**
	 * Returns an array of reservations matching all between the start and end timestamps.
	 * @param startTime low boundary which to use in the search for reservations
	 * @param endTime high boundary which to use in the search for reservations
	 * @return Either return a 204: no content response via WebApplicationException if no reservations match 
	 * the range. A 500 is returned for an internal exception and 400 for bad request. Otherwise a json array of 
	 * reservations is returned.
	 * 
	 * Note that the full calendar uses start and end query parameters: 
	 * server.com/reservations?start=unixtime&end=unixtime
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Reservation> listReservations(@QueryParam("start") String startTime, @QueryParam("end") String endTime) {

		new RestRequestParameterValidator()
				.add("start", startTime, NOT_NEGATIVE_OR_ZERO_AS_STRING)
				.add("end", endTime, NOT_NEGATIVE_OR_ZERO_AS_STRING)
				.validate();

		Date start = getDateFrom(startTime);
		Date end = getDateFrom(endTime);

		List<Reservation> reservations;
		try {
			reservations = reservationService.findReservations(start, end);

		} catch (ClassNotFoundException | SQLException e) {

			logger.error("Exception while finding reservations.\n{}", e);
			throw new WebApplicationException(500);
		}

		if (reservations.size() == 0) {

			logger.info("No content 204 between start ({}) and end ({}).", startTime, endTime);
			throw new WebApplicationException(204);
		}

		return reservations;
	}

}
