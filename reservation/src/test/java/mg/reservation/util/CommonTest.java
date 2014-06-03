package mg.reservation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
		Date parsedDate = Common.getDateFrom("1401429120000");
		assertEquals("Timestamps should be equal", expectedDate.getTime(), parsedDate.getTime());

		parsedDate = Common.getDateFrom((String) null);
		assertNull("using a null argument should return null", parsedDate);

		parsedDate = Common.getDateFrom("");
		assertNull("using an invalid argument should return null", parsedDate);

		parsedDate = Common.getDateFrom("NOTAVALIDNUMBER");
		assertNull("using an invalid argument should return null", parsedDate);
	}

	@Test
	public void testParsingLong() {

		Long longCandidate = Common.getLong(null);
		assertNull(longCandidate);

		longCandidate = Common.getLong("");
		assertNull(longCandidate);

		longCandidate = Common.getLong("a");
		assertNull(longCandidate);

		longCandidate = Common.getLong("0");
		assertNotNull(longCandidate);
		assertEquals(new Long(0), longCandidate);

		longCandidate = Common.getLong("10");
		assertNotNull(longCandidate);
		assertEquals(new Long(10), longCandidate);
	}

	@Test
	public void testAnyNull() {

		boolean isAnyNull = Common.isAnyNull("");
		assertFalse(isAnyNull);

		isAnyNull = Common.isAnyNull((String) null, "");
		assertTrue(isAnyNull);

		isAnyNull = Common.isAnyNull("", (String) null);
		assertTrue(isAnyNull);

		isAnyNull = Common.isAnyNull((String) null, (String) null);
		assertTrue(isAnyNull);

		isAnyNull = Common.isAnyNull("", "");
		assertFalse(isAnyNull);
	}
}
