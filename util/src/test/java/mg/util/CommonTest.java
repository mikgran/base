package mg.util;

import static java.util.Arrays.asList;
import static mg.util.Common.convertFullCalendarDateToJavaDate;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.getDateFrom;
import static mg.util.Common.getDateFromFCDS;
import static mg.util.Common.getFirstInstantOfTheWeek;
import static mg.util.Common.getLastInstantOfTheWeek;
import static mg.util.Common.getLong;
import static mg.util.Common.hasContent;
import static mg.util.Common.isAnyNull;
import static mg.util.Common.yyyyMMddHHmmFormatter;
import static mg.util.Common.yyyyMMddHHmmssFormatter;
import static mg.util.Common.zipWithIndex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommonTest {

    public class TestException extends Exception {
        private static final long serialVersionUID = 1L;
        public TestException(String message) {
            super(message);
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAnyNull() {

        boolean isAnyNull = isAnyNull("");
        assertFalse(isAnyNull);

        isAnyNull = isAnyNull((String) null, "");
        assertTrue(isAnyNull);

        isAnyNull = isAnyNull("", (String) null);
        assertTrue(isAnyNull);

        isAnyNull = isAnyNull((String) null, (String) null);
        assertTrue(isAnyNull);

        isAnyNull = isAnyNull("", "");
        assertFalse(isAnyNull);
    }

    @Test
    public void testConvertFullCalendarDateString() {

        String s = "Sun Jun 01 2014 00:00:00 GMT+0300 (Eastern European Daylight Time)";

        String candidateString = convertFullCalendarDateToJavaDate(s);

        assertNotNull(candidateString);
        assertEquals("Sun Jun 01 2014 00:00:00 GMT+03:00 (Eastern European Daylight Time)", candidateString);

        candidateString = convertFullCalendarDateToJavaDate((String) null);
        assertNull(candidateString);
    }

    @Test
    public void testDateToLocalDateTime() throws ParseException {

        LocalDateTime localDateTime = LocalDateTime.of(2010, 10, 10, 12, 30);
        Date date = Common.yyyyMMddHHmmFormatter.parse("2010-10-10 12:30");
        LocalDateTime localDateTimeCandidate = Common.toLocalDateTime(date);
        assertNotNull(localDateTimeCandidate);
        assertEquals("the converted localDateTime should equal to: ", localDateTime, localDateTimeCandidate);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("date can not be null.");
        Common.toLocalDateTime(null);
    }

    @Test
    public void testFlattenToStream() {

        // Collection of Collections of Objects
        // "{{A},{B,C,D},{},{E,F,G,H},{I}}"
        List<List<String>> listOfListsOfStrings = asList(asList("A"),
                                                         asList("B", "C", "D"),
                                                         asList(""),
                                                         asList("E", "F", "G", "H"),
                                                         asList("I"));

        String flattenedStringsJoined = listOfListsOfStrings.stream()
                                                            .flatMap(collection -> flattenToStream(collection))
                                                            .filter(object -> object instanceof String)
                                                            .map(String.class::cast)
                                                            .collect(Collectors.joining(","));

        assertEquals("the listOfListsOfStrings should be equal after flattening and joining with comma to: ", "A,B,C,D,,E,F,G,H,I", flattenedStringsJoined);
    }

    @Test
    public void testGetDateFromFC() throws ParseException {

        String s = "Sun Jun 08 2014 00:00:00 GMT+0300 (Eastern Europe Daylight Time)";

        Date date = getDateFromFCDS(s);
        assertNotNull(date);
        assertEquals(1402174800000L, date.getTime());
    }

    @Test
    public void testGetFirstInstantOfTheWeek() throws ParseException {
        Date date = yyyyMMddHHmmssFormatter.parse("2010-01-20 00:00:00");
        Date firstInstantOfTheWeek1 = getFirstInstantOfTheWeek(date, 1);

        Date expectedDate = yyyyMMddHHmmssFormatter.parse("2010-01-04 00:00:00"); // week 1 on 2010 was monday 4th january.

        // 1st week: 2010-01-01 00:00
        assertEquals(expectedDate.getTime(), firstInstantOfTheWeek1.getTime());
    }

    @Test
    public void testGetLastInstantOfTheWeek() throws ParseException {

        Date date = yyyyMMddHHmmssFormatter.parse("2010-01-20 00:00:00");
        Date lastInstantOfTheWeek1 = getLastInstantOfTheWeek(date, 1);

        Date expectedDate = yyyyMMddHHmmssFormatter.parse("2010-01-10 23:59:59"); // week 1 on 2010 was monday 4th january.

        // 1st week: 2010-01-01 00:00
        assertEquals(expectedDate.getTime(), lastInstantOfTheWeek1.getTime());
    }

    @Test
    public void testGettingDateFromUnixTimestamp() throws Exception {

        Date expectedDate = yyyyMMddHHmmFormatter.parse("2014-05-30 08:52:00");
        Date parsedDate = getDateFrom("1401429120000");
        assertEquals("Timestamps should be equal", expectedDate.getTime(), parsedDate.getTime());

        parsedDate = getDateFrom((String) null);
        assertNull("using a null argument should return null", parsedDate);

        parsedDate = getDateFrom("");
        assertNull("using an invalid argument should return null", parsedDate);

        parsedDate = getDateFrom("NOTAVALIDNUMBER");
        assertNull("using an invalid argument should return null", parsedDate);
    }

    @Test
    public void testHasContent() {

        assertTrue("'content' should return true", hasContent("content"));
        assertFalse("'null' should return false", hasContent((String) null));
    }

    @Test
    public void testHasContentArray() {

        String[] stringArrayNull = null;
        String[] stringArrayZeroSize = new String[0];
        String[] stringArraySize1NullContent = new String[1];
        String[] stringArraySize2NonFirstHasContentSecondDoesNot = {"1", null};
        String[] stringArraySize3AllHaveContent = {"1", "2", "3"};

        assertFalse("null array should return false", hasContent((String[]) stringArrayNull));
        assertFalse("initialized array zero length should return false", hasContent(stringArrayZeroSize));
        assertFalse("initialized array length 1 but null content should return false", hasContent(stringArraySize1NullContent));
        assertFalse("initialized array length 2 one of 2 elements null should return false", hasContent(stringArraySize2NonFirstHasContentSecondDoesNot));
        assertTrue("initialized array length 3 all elements have content should return true", hasContent(stringArraySize3AllHaveContent));
    }

    @Test
    public void testHasContentArrayList() {

        ArrayList<String> arrayListNull = null;
        ArrayList<String> arrayListNoContent = new ArrayList<String>();
        ArrayList<String> arrayList1Element = new ArrayList<String>();
        arrayList1Element.add("1");

        assertFalse("null list should return false", hasContent((ArrayList<String>) arrayListNull));
        assertFalse("empty list should return false", hasContent(arrayListNoContent));
        assertTrue("list with at least 1 element should return true", hasContent(arrayList1Element));
    }

    @Test
    public void testLocalDateTimeToDate() throws ParseException {

        LocalDateTime localDateTime = LocalDateTime.of(2010, 10, 10, 12, 30);
        Date date = Common.yyyyMMddHHmmFormatter.parse("2010-10-10 12:30");
        Date dateCandidate = Common.toDate(localDateTime);

        assertNotNull(dateCandidate);
        assertEquals("the converted date should equal to: ", date, dateCandidate);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("localDateTime can not be null.");
        Common.toDate(null);
    }

    @Test
    public void testParsingLong() {

        Long longCandidate = getLong(null);
        assertNull(longCandidate);

        longCandidate = getLong("");
        assertNull(longCandidate);

        longCandidate = getLong("a");
        assertNull(longCandidate);

        longCandidate = getLong("0");
        assertNotNull(longCandidate);
        assertEquals(new Long(0), longCandidate);

        longCandidate = getLong("10");
        assertNotNull(longCandidate);
        assertEquals(new Long(10), longCandidate);
    }

    @Test(expected = TestException.class)
    public void testUnwrapCauseAndRethrow() throws Exception {
        try {
            TestException causedBy = new TestException("test exception");
            throw new RuntimeException(causedBy);
        } catch (Exception e) {
            Common.unwrapCauseAndRethrow(e);
        }
    }

    @Test
    public void testZip() {

        Stream<String> streamA = Arrays.asList("0", "1", "2").stream();
        List<String> listB = Arrays.asList("A", "B", "C");
        listB.sort((a, b) -> a.compareTo(b) * -1);
        Stream<String> streamB = listB.stream();

        String result = Common.zip(streamA, streamB, (a, b) -> a.toString() + b.toString())
                              .collect(Collectors.joining(", "));

        assertNotNull(result);
        assertEquals("list of Strings: A, B, C after zipping with index should equal to: ", "0C, 1B, 2A", result);
    }

    @Test
    public void testZipWithIndexAndTuples() {

        Stream<String> stream = Arrays.asList("A", "B", "C").stream();

        String result = zipWithIndex(stream).map(t -> t.getS().toString() + t.getT().toString())
                                            .collect(Collectors.joining(", "));

        assertNotNull(result);
        assertEquals("list of Strings: A, B, C after zipping with index should equal to: ", "0A, 1B, 2C", result);

    }

}
