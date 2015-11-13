package mg.util;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

public class Common {

    // Breaking the camel case here for clarity sakes. So sue me. TOIMPROVE:
    // replace with joda time at some point.
    public static final SimpleDateFormat yyyyMMddHHmmFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat yyyyMMddHHmmssFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat EEEMMMddyyyyHHmmsszzzFormatter = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzz", Locale.ENGLISH);

    public static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";

    /**
     * Test whether any given object is null.
     * 
     * @param objects
     *            the objects to be tested.
     * @return true if at least of the objects were null, false otherwise.
     */
    public static boolean isAnyNull(Object... objects) {

        if (objects != null &&
            objects.length > 0) {

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
        } catch (Exception ignored) {
            // TOIMPROVE: log the error with a default logger.
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
                } catch (Exception ignored) {
                    // TOIMPROVE: log the error with a default logger.
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
        } catch (Exception ignored) {
            // TOIMPROVE: log the error with a default logger.
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
                } catch (Exception ignored) {
                    // TOIMPROVE: log the error with a default logger.
                }
            }
        }
    }

    /**
     * Transforms string type unix timestamp to a Date object.
     * 
     * @param unixTimeStamp
     *            The string to convert.
     * @return Date object representing the timestamp if successful, null
     *         otherwise.
     */
    public static Date getDateFrom(String unixTimeStamp) {

        try {
            return new Date(Long.parseLong(unixTimeStamp));

        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Parses a Date object from fullCalendar date String. Converts the
     * fullCalendar date string into java compatible string by adding a colon
     * into the timezone part of the string.
     * 
     * @param fullCalendarDateString
     *            the string to convert to a Date object.
     * @return If successful the Date represented by the String, otherwise null
     *         on parse errors.
     */
    public static Date getDateFromFCDS(String fullCalendarDateString) {

        try {

            if (hasContent(fullCalendarDateString)) {

                String javaDateString = Common.convertFullCalendarDateToJavaDate(fullCalendarDateString);

                return EEEMMMddyyyyHHmmsszzzFormatter.parse(javaDateString);
            }

        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Converts a fullCalendar date String to a java compatible and parseable
     * date String. Finds the GMT+XXYY and converts that portion of the String
     * into GMT+XX:YY.
     * 
     * The fullCalendar creates: Sun Jun 01 2014 00:00:00 GMT+0300 (Eastern
     * Europe Daylight Time) Proper java date form : Sun Jun 01 2014 00:00:00
     * GMT+03:00 (Eastern European Daylight Time)
     * 
     * Proper java date form is parseable by the SimpleDateFormatter(
     * "EEE MMM dd yyyy HH:mm:ss zzz").
     * 
     * @param s
     *            The string to convert.
     * @return The converted string with the proper ':' in the time zone. If
     *         unable to
     */
    public static String convertFullCalendarDateToJavaDate(String s) {

        if (!hasContent(s)) {
            return null;
        }

        String pattern = "(.*)(GMT\\+\\d\\d)(.*)";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(s);

        if (m.find()) {
            return String.format("%s%s:%s", m.group(1), m.group(2), m.group(3));
        }

        return null;
    }

    /**
     * Transforms an object into Long using toString to get a candidate number
     * as string and then transforming that via Long.parseLong to an integer.
     * 
     * @param object
     *            the candidate object to transform into integer.
     * @return a Long if object was transformable otherwise a null.
     */
    public static Long getLong(Object object) {
        try {
            return Long.parseLong(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Tests whether a given String has content.
     * 
     * @param s
     *            the parameter to test for.
     * @return true if the parameter s was not null and had content by having
     *         length higher than zero.
     */
    public static boolean hasContent(String s) {
        if (s != null &&
            s.length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Tests whether a given array of strings has content.
     * 
     * @param initializationSqlStrings
     *            the array of strings to test against
     * @return true if sa was not null, had a length above zero and if every
     *         element had content with length above zero.
     */
    public static boolean hasContent(Object[] sa) {

        if (sa == null || sa.length == 0) {
            return false;
        }

        boolean validity = true;

        for (Object s : sa) {
            if (s == null) {
                validity = false;
            }
        }

        return validity;
    }

    /**
     * Tests whether a given List has content.
     * 
     * @param list
     *            a List<?> the list to test
     * @return true if list was not null and if it contained at least one
     *         element, false otherwise.
     */
    public static boolean hasContent(List<?> list) {

        if (list == null || list.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Returns a Date representing the last second of the given week.
     * 
     * @param weekNumber
     *            The week to floor.
     * @param date
     *            the year to fetch the week for.
     * @return Whe instant when the week starts. A null is returned if unable to
     *         get the first instant of the given week.
     */
    public static Date getFirstInstantOfTheWeek(Date date, int weekNumber) {

        Date firstInstantOfTheWeek = null;

        if (weekNumber <= 0) {
            return firstInstantOfTheWeek;
        }

        try {
            DateTime startOfTheWeek = new DateTime(date).withWeekOfWeekyear(weekNumber)
                                                        .withDayOfWeek(1);
            firstInstantOfTheWeek = startOfTheWeek.withTime(00, 00, 00, 000)
                                                  .toDate();

        } catch (Exception ignoredAndNotLogged) {
        }

        return firstInstantOfTheWeek;
    }

    /**
     * Returns a Date representing the last second of the given week.
     * 
     * @param weekNumber
     *            The week to max.
     * @param date
     *            the year to fetch the week for.
     * @return The last second of the week. a null is returned if unable to get
     *         the last instant of the given week.
     */
    public static Date getLastInstantOfTheWeek(Date date, int weekNumber) {

        Date lastInstantOfTheCurrentWeek = null;

        if (weekNumber <= 0) {
            return lastInstantOfTheCurrentWeek;
        }

        try {
            DateTime lastDayFirstInstant = new DateTime(date).withWeekOfWeekyear(weekNumber)
                                                             .withDayOfWeek(7);
            lastInstantOfTheCurrentWeek = lastDayFirstInstant.withTime(23, 59, 59, 000)
                                                             .toDate();

        } catch (Exception ignoredAndNotLogged) {
        }

        return lastInstantOfTheCurrentWeek;
    }
}
