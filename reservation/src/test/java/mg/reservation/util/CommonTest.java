package mg.reservation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommonTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testGettingDateFromUnixTimestamp() throws Exception {

		Date expectedDate = Common.yyyyMMddHHmmFormatter.parse("2014-05-30 08:52:00");
		Date parsedDate = Common.getDateForUnixTimeStamp("1401429120000");
		assertEquals("Timestamps should be equal", expectedDate.getTime(), parsedDate.getTime());
		
		parsedDate = Common.getDateForUnixTimeStamp((String)null);
		assertNull("using a null argument should return null", parsedDate);
		
		parsedDate = Common.getDateForUnixTimeStamp("");
		assertNull("using an invalid argument should return null", parsedDate);
		
		parsedDate = Common.getDateForUnixTimeStamp("NOTAVALIDNUMBER");
		assertNull("using an invalid argument should return null", parsedDate);
	}
	
	
}
