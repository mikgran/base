package mg.reservation.dao;

import java.io.IOException;
import java.util.Properties;

import mg.reservation.util.Config;

public class TestConfig extends Config {
	
	public static String TEST_CONFIG_PROPERTIES = "test-config.properties";
	
	@Override
	public Properties loadProperties() throws IOException {
		return loadProperties(TEST_CONFIG_PROPERTIES);
	}
}
