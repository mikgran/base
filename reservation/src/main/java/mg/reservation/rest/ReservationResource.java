package mg.reservation.rest;

import static mg.reservation.util.Common.getDateFrom;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/reservation")
public class ReservationResource {

	private Logger logger = LoggerFactory.getLogger(ReservationResource.class);
	private ReservationService reservationService = null;

	public ReservationResource() throws IOException { // allowing the missing configuration to fall back to OS
		reservationService = new ReservationService(new DBConfig(new Config()));

		PropertyConfigurator.configure("log4j.properties");
	}

	@GET
	@Produces("text/plain")
	public String getMessage() {
		return "Hello Rest World";
	}

	@GET
	@Path("query")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Reservation> restJson(@QueryParam("start") String startTime, @QueryParam("end") String endTime) {

		Date start = getDateFrom(startTime);
		Date end = getDateFrom(endTime);

		if (start == null || end == null) {
			logger.error("call parameters null: start ({}), end ({})", start, end);
			throw new WebApplicationException("Start or end time can not be null.", 400);
		}

		List<Reservation> reservations;
		try {

			reservations = reservationService.findReservations(start, end);

		} catch (ClassNotFoundException | SQLException e) {

			logger.error("Exception while finding reservations.\n{}", e);
			throw new WebApplicationException(500);
		}

		if (reservations.size() > 0) {
			return reservations;
		}

		// no content reply: throw new WebApplicationException();
		return new ArrayList<Reservation>(); // TODO replace with no content reply
	}

}