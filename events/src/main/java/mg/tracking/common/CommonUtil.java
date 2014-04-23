package mg.tracking.common;

import java.io.Closeable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonUtil {

	private static final Logger logger = LogManager.getLogger(CommonUtil.class);

	// some people shun the statics for bad testability, can be replaced with instantiating the util classes instead
	public static void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {
			logger.warn("Unable to close a resource.", e);
		}
	}
}
