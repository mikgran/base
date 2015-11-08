package mg.reservation;

import java.io.IOException;

import mg.reservation.config.ReservationServletModule;
import mg.reservation.service.ReservationService;
import mg.reservation.service.ReservationServiceImpl;
import mg.util.TestConfig;
import mg.util.db.DBConfig;

import com.google.inject.Module;

public class ReservationServletTestModule extends ReservationServletModule implements Module {

	String testDbName = "reservationtest2";

	@Override
	public ReservationService provideDbConfig() throws IOException {

		return new ReservationServiceImpl(new DBConfig(new TestConfig(testDbName)));
	}

}
