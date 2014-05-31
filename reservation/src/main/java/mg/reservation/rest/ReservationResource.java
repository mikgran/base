package mg.reservation.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/hello")
public class ReservationRest {

	@GET
	@Produces("text/plain")
	public String getMessage() {
		return "Hello Rest World";
	}
}