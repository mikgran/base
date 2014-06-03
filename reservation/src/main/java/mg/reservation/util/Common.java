package mg.reservation.util;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	 * Silently closes resources implementing the AutoCloseable interface.
	 * 
	 * @param closeable
	 *            the resource to be closed.
	 */
	public static void close(AutoCloseable... autoCloseables) {
		if (autoCloseables != null) {
			for (AutoCloseable autoCloseable : autoCloseables) {
				try {
					autoCloseable.close();
					// mvp ignored, calling close on the autoCloseable may cause nondeterministic behavior according to the javadoc.
				} catch (Exception ignored) {
				}
			}
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

	/**
	 * Silently closes resources implementing the Closeable interface.
	 * 
	 * @param closeable
	 *            the resource to be closed.
	 */
	public static void close(Closeable... closeables) {
		if (closeables != null) {
			for (Closeable closeable : closeables) {
				try {
					closeable.close();
					// mvp ignored, calling close on the autoCloseable may cause nondeterministic behavior according to the javadoc.
				} catch (Exception ignored) {
				}
			}
		}
	}

	/**
	 * Transforms string type unix timestamp to a Date object.
	 * @param unixTimeStamp The string to convert.
	 * @return Date object representing the timestamp if successful, null otherwise.
	 */
	public static Date getDateFrom(String unixTimeStamp) {

		try {
			return new Date(Long.parseLong(unixTimeStamp));

		} catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * Transforms an object into Integer using toString to get a
	 * candidate number as string and then transforming that via
	 * Integer.parseInt to an integer.
	 * @param object the candidate object to transform into integer.
	 * @return a Integer if object was transformable otherwise a null.
	 */
	public static Integer getInteger(Object object) {
		try {
			return Integer.parseInt(object.toString());
		} catch (Exception ignored) {
			return null;
		}
	}
}
