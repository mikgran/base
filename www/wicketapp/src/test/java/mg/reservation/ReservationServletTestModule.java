package mg.reservation;

import java.io.IOException;

import mg.reservation.config.ReservationServletModule;
import mg.reservation.dao.TestConfig;
import mg.reservation.db.DBConfig;
import mg.reservation.service.ReservationService;
import mg.reservation.service.ReservationServiceImpl;

import com.google.inject.Module;

public class ReservationServletTestModule extends ReservationServletModule implements Module {

	String testDbName = "reservationtest2";
	
	@Override
	public ReservationService provideDbConfig() throws IOException {
			
		return new ReservationServiceImpl(new DBConfig(new TestConfig(testDbName)));
	}
	
}
