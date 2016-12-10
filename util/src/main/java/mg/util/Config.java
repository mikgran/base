package mg.util;

import static mg.util.validation.Validator.validateNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A generic configuration loading class. Holds properties until refreshed via
 * the appropriate methods.
 */
public class Config {

    public static String CONFIG_PROPERTIES = "config.properties";

    private static Properties properties = null;

    /**
     * Loads the properties currently found in the config.properties file. The
     * loaded properties are kept and returned again with subsequent calls to
     * this method.
     *
     * @return the properties read from the file.
     * @throws IOException
     *             If the file can not be found or read.
     */
    public Properties loadProperties() throws IOException {
        return loadProperties(CONFIG_PROPERTIES);
    }

    /**
     * Loads the properties currently found in a given file. The loaded
     * properties are kept and returned with subsequent calls to this method.
     *
     * @param fileName
     *            The file to read the properties from.
     * @return Set of properties in the file.
     * @throws IOException
     *             If the file can not be found or read.
     */
    public Properties loadProperties(String fileName) throws IOException {

        validateNotNull("fileName", fileName);

        if (properties == null) {
            properties = new Properties();

            try (InputStream inputStream = new FileInputStream(fileName)) {

                properties.load(inputStream);
            }
        }

        return properties;
    }

    /**
     * Reloads the config.properties from the disk.
     *
     * @return the freshly loaded properties.
     * @throws IOException
     *             If unable to read the config.properties file.
     */
    public Properties refreshProperties() throws IOException {
        properties = null;
        return loadProperties();
    }

    /**
     * Reloads the properties from disk.
     *
     * @param file
     *            the file to reload the properties from.
     *
     * @return the freshly loaded properties.
     * @throws IOException
     *             If unable to read the config.properties file.
     */
    public Properties refreshProperties(String file) throws IOException {
        properties = null;
        return loadProperties(file);
    }

}
