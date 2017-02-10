package mg.util;

import java.io.IOException;
import java.util.Properties;

import mg.util.db.DBConfig;

public class TestConfig extends Config {

    private String dbName = null;

    public TestConfig() {
    }

    public TestConfig(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public Properties loadProperties() throws IOException {
        Properties properties = loadProperties("test-config.properties");
        if (Common.hasContent(dbName)) {
            properties.setProperty(DBConfig.DB_URL, dbName);
        }
        return properties;
    }
}
