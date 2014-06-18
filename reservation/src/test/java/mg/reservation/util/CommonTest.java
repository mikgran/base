package mg.reservation.util;

import static mg.reservation.util.Common.yyyyMMddHHmmssFormatter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
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

	@Test
	public void testHasContent() {

		boolean hasContent = Common.hasContent("content");
		assertTrue("'content' should return true", hasContent);

		hasContent = Common.hasContent((String) null);
		assertFalse("'null' should return false", hasContent);
	}

	@Test
	public void testGetDateFromFC() throws ParseException {

		String s = "Sun Jun 08 2014 00:00:00 GMT+0300 (Eastern Europe Daylight Time)";

		Date date = Common.getDateFromFCDS(s);
		assertNotNull(date);
		assertEquals(1402174800000L, date.getTime());
	}

	@Test
	public void testConvertFullCalendarDateString() {

		String s = "Sun Jun 01 2014 00:00:00 GMT+0300 (Eastern European Daylight Time)";

		String candidateString = Common.convertFullCalendarDateToJavaDate(s);

		assertNotNull(candidateString);
		assertEquals("Sun Jun 01 2014 00:00:00 GMT+03:00 (Eastern European Daylight Time)", candidateString);

		candidateString = Common.convertFullCalendarDateToJavaDate((String) null);
		assertNull(candidateString);
	}

	@Test
	public void testGetFirstInstantOfTheWeek() throws ParseException {
		Date date = dateFrom("2010-01-20 00:00:00");
		Date firstInstantOfTheWeek1 = Common.getFirstInstantOfTheWeek(date, 1);

		Date expectedDate = dateFrom("2010-01-04 00:00:00"); // week 1 on 2010 was monday 4th january.

		// 1st week: 2010-01-01 00:00
		assertEquals(expectedDate.getTime(), firstInstantOfTheWeek1.getTime());
	}

	@Test
	public void testGetLastInstantOfTheWeek() throws ParseException {

		Date date = dateFrom("2010-01-20 00:00:00");
		Date lastInstantOfTheWeek1 = Common.getLastInstantOfTheWeek(date, 1);

		Date expectedDate = dateFrom("2010-01-10 23:59:59"); // week 1 on 2010 was monday 4th january.

		// 1st week: 2010-01-01 00:00
		assertEquals(expectedDate.getTime(), lastInstantOfTheWeek1.getTime());
	}

	private Date dateFrom(String dateString) throws ParseException {
		return yyyyMMddHHmmssFormatter.parse(dateString);
	}
}
