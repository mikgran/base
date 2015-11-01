package mg.angular.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.reservation.db.Reservation;
import mg.reservation.rest.ReservationResource;

@Path("/contactlist")
public class ReservationManager {

	private Logger logger = LoggerFactory.getLogger(ReservationResource.class);

	public ReservationManager() throws IOException {


		PropertyConfigurator.configure("log4j.properties");
	}

	@GET
	@Produces("text/html; charset=UTF-8")
	@Path("/hello")
	public Response helloWorld() {

		logger.info("Hello world");
		
		String responseMessage = "Hello World!";

		return Response.status(200)
				.entity(responseMessage)
				.build();
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response listContacts() {

		return null;
	}

	@POST
	@Produces({MediaType.APPLICATION_JSON})
	public List<Reservation> setReservation() {

		return null;
	}

}
