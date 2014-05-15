package mg.reservation.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api")
public class ReservationRestApplication extends ResourceConfig {
	public ReservationRestApplication() {
		packages("mg.reservation.rest");
	}
}

