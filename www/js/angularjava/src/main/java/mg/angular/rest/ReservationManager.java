package mg.angular.rest;

import static mg.reservation.util.Common.yyyyMMddHHmmFormatter;
import static mg.reservation.validation.rule.ValidationRule.NUMBER_OF_CHARACTERS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.reservation.db.Reservation;
import mg.reservation.rest.ReservationResource;
import mg.reservation.util.Common;
import mg.reservation.validation.RestRequestParameterValidator;

@Path("/reservations")
public class ReservationManager {

	private Logger logger = LoggerFactory.getLogger(ReservationResource.class);
	// private ReservationServiceImpl reservationService = null;

	public ReservationManager() throws IOException {

		// reservationService = new ReservationServiceImpl(new DBConfig(new
		// Config()));

		PropertyConfigurator.configure("log4j.properties");
	}

	@GET
	@Produces("text/html; charset=UTF-8")
	@Path("/hello")
	public Response helloWorld() {

		String responseMessage = "Hello World!";

		return Response.status(200)
				.entity(responseMessage)
				.build();
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<Reservation> listReservations(@QueryParam("start") String startTime, @QueryParam("end") String endTime) {

		logger.info("listReservations(start:{}, end:{})", startTime, endTime);

		new RestRequestParameterValidator().add("start", startTime, NUMBER_OF_CHARACTERS.atLeast(6))
				.add("end", endTime, NUMBER_OF_CHARACTERS.atLeast(6))
				.validate();

		Date start = Common.getDateFrom(startTime);
		Date end = Common.getDateFrom(endTime);

		if (start == null && end == null) {
			start = Common.getDateFromFCDS(startTime);
			end = Common.getDateFromFCDS(endTime);
		}

		List<Reservation> reservations;
		try {
			// reservations = reservationService.findReservations(start, end);

			// Mock one reservation for now:
			String id = "AC1";
			String startTime2 = "2010-10-01 00:00";
			String endTime2 = "2010-10-01 03:00";
			String reserver = "Person";
			String resource = "Alpha";
			String title = "title";
			String description = "description";

			reservations = new ArrayList<Reservation>();
			reservations.add(new Reservation(id, resource, reserver, yyyyMMddHHmmFormatter.parse(startTime2), yyyyMMddHHmmFormatter.parse(endTime2), title, description));

		} catch (Exception e) {// ClassNotFoundException | SQLException e) {

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
