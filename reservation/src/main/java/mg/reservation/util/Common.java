package mg.reservation.util;

import java.io.Closeable;
import java.text.SimpleDateFormat;

public class Common {
	
	// Breaking the camel case here for clarity sakes. So sue me.
	public static final SimpleDateFormat yyyyMMddHHmmFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public static void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (Exception ignored) {
			// TODO: can be logged, if necessary
		}		
	}
	
	
}
