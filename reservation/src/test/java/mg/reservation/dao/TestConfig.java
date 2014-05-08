package mg.reservation.dao;

import java.io.IOException;
import java.util.Properties;

import mg.reservation.db.DBConfig;
import mg.reservation.util.Config;

public class TestConfig extends Config {

	public String testDbUrl = "";

	public TestConfig(String testDbUrl) {
		this.testDbUrl = "jdbc:mysql://localhost/" + testDbUrl;
	}

	@Override
	public Properties loadProperties() throws IOException {
		Properties properties = loadProperties("test-config.properties");
		properties.setProperty(DBConfig.DB_URL, testDbUrl);
		return properties;
	}
}
