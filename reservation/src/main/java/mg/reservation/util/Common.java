package mg.reservation.util;

import java.io.Closeable;
import java.text.SimpleDateFormat;

public class Common {

	// Breaking the camel case here for clarity sakes. So sue me.
	public static final SimpleDateFormat yyyyMMddHHmmFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * Test whether any given object is null.
	 * 
	 * @param objects
	 *            the objects to be tested.
	 * @return true if at least of the objects were null, false otherwise.
	 */
	public static boolean isAnyNull(Object... objects) {

		if (objects != null && objects.length > 0) {

			for (int i = 0; i < objects.length; i++) {

				if (objects[i] == null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Silently closes a resource implementing the AutoCloseable interface.
	 * 
	 * @param closeable
	 *            the resource to be closed.
	 */
	public static void close(AutoCloseable autoCloseable) {
		try {
			autoCloseable.close();
			// mvp ignored, calling close on the autoCloseable may cause nondeterministic behavior according to the javadoc.
		} catch (Exception ignored) {
		}
	}

	/**
	 * Silently closes a resource implementing the Closeable interface.
	 * 
	 * @param closeable
	 *            the resource to be closed.
	 */
	public static void close(Closeable closeable) {
		try {
			closeable.close();
			// mvp ignored
		} catch (Exception ignored) {
		}
	}

}
