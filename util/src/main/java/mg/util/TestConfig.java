package mg.util;

import java.io.IOException;
import java.util.Properties;

public class TestConfig extends Config {

    @Override
    public Properties loadProperties() throws IOException {
        Properties properties = loadProperties("test-config.properties");
        return properties;
    }
}

