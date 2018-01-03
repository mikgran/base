package mg.util;

import static mg.util.validation.Validator.validateNotNull;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.joda.time.DateTime;

import mg.util.functional.function.ThrowingFunction;

public class Common {

    // Breaking the camel case here for clarity sakes. So sue me.
    public static final String DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
    public static final SimpleDateFormat EEEMMMddyyyyHHmmsszzzFormatter = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzz", Locale.ENGLISH);
    public static final SimpleDateFormat yyyyMMddHHmmFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat yyyyMMddHHmmssFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Returns a function that converts an object into Class<E>.
     * @param clazz the non-null Class<E> to convert the Object into.
     * @return a function that converts Object o into E. If casting is not possible, a null is returned.
     */
    public static <E> Function<Object, E> asInstanceOf(Class<E> clazz) {
        validateNotNull("clazz", clazz);
        return o -> clazz.isInstance(o) ? clazz.cast(o) : null;
    }

    /**
     * Returns a throwing function that converts an object into Class<E>.
     * @param clazz the non-null Class<E> to convert the Object into.
     * @return a function that converts Object o into E. If casting is not possible, a null is returned.
     */
    public static <X extends Exception, E> ThrowingFunction<Object, E, X> asInstanceOfT(Class<E> clazz) throws X {
        validateNotNull("clazz", clazz);
        return o -> clazz.isInstance(o) ? clazz.cast(o) : null;
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
     * Flattens a Collection of Collections of Objects:
     * {{A},{B,C,D},{},{E,F,G,H},{I}} ->
     * {A, B, C, D, , E, F, G, H, I}
     * @param collection the collection of collections to flatten
     * @return the flattened collection of collections as Stream of objects.
     */
    public static Stream<Object> flattenToStream(Collection<?> collection) {
        return collection.stream()
                         .flatMap(item -> item instanceof Collection<?> ? flattenToStream((Collection<?>) item) : Stream.of(item));
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
     * Tests whether a given number is not a null and has a nonzero value.
     * @param l the number to test.
     * @return true if given number was not null and if it was a nonzero value. NOTE: intentional zero value case is not covered with this method: mainly used for IDs (that should be nonzero values).
     */
    public static boolean hasContent(Long l) {

        if (l == null || l == 0) {
            return false;
        }
        return true;
    }

    /**
     * Tests whether a given array of strings has content.
     *
     * @param initializationSqlStrings
     *            the array of strings to test against
     * @return true if sa was not null, had a length above zero and if every
     *         element had non null content.
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
     * Convenience Function for filtering and casting objects into given type: objects.stream().filter(o -> o instanceof Clazz.class).map(o -> (Clazz)o);<br/><br/>
     * <pre>
     * Usage: objects.stream()
     *               .flatMap(Common.instancesOf(Clazz.class)
     *               .forEach(clazzObject -> {});
     * </pre>
     * @param cls the class to test stream objects against and to cast to.
     * @return Stream.of(object) or Stream.empty() in case not castable.
     */
    public static <E> Function<Object, Stream<E>> instancesOf(Class<E> cls) {
        return o -> cls.isInstance(o) ? Stream.of(cls.cast(o)) : Stream.empty();
    }

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

    // TOIMPROVE: there's got to be a better way of doing this.
    public static boolean isInterchangeable(Object boxedNumber, Class<?> numberType) {

        if (boxedNumber == null || numberType == null) {
            return false;
        }

        if ((boxedNumber instanceof Integer && Integer.TYPE == numberType) ||
            (boxedNumber instanceof Long && Long.TYPE == numberType) ||
            (boxedNumber instanceof Float && Float.TYPE == numberType) ||
            (boxedNumber instanceof Double && Double.TYPE == numberType) ||
            (boxedNumber instanceof Short && Short.TYPE == numberType) ||
            (boxedNumber instanceof Byte && Byte.TYPE == numberType) ||
            (boxedNumber instanceof Character && Character.TYPE == numberType)) {

            return true;
        }
        return false;
    }

    public static <T> Stream<T> iteratorToFiniteStream(Iterator<T> iterator, boolean parallel) {
        final Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    // breaking naming convention intentionally here.
    public static Stream<String> splitToStream(List<String> listOfStrings, String splitter) {
        if (!hasContent(listOfStrings)) {
            return Stream.empty();
        }
        validateNotNull("splitter", splitter);
        return listOfStrings.stream()
                            .flatMap(s -> splitToStream(s, splitter));
    }

    public static Stream<String> splitToStream(String string, String splitter) {
        validateNotNull("string", string);
        validateNotNull("splitter", splitter);
        return Arrays.stream(string.split(splitter));
    }

    public static Date toDate(LocalDateTime localDateTime) {
        validateNotNull("localDateTime", localDateTime);
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        validateNotNull("date", date);
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Rethrows an causing exception received from throwable.getCause() or
     * if no cause present the exception itself is rethrown.
     * <br><br>
     * Provided for ThrowingConsumer and to circumvent the Consumer which
     * does not throw an exception.
     * @param e The exception to unwrap and rethrow.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Exception> void unwrapCauseAndRethrow(E e) throws E {
        if (e.getCause() == null) {
            throw e;
        }

        throw (E) e.getCause();
    }

    public static <A, B, C> Stream<C> zip(List<A> listA, List<B> listB, BiFunction<A, B, C> zipper) {
        validateNotNull("listA", listA);
        validateNotNull("listB", listB);
        return zip(listA.stream(), listB.stream(), zipper);
    }

    // borrowing someone's solution boldly right here.
    public static <A, B, C> Stream<C> zip(Stream<A> streamA, Stream<B> streamB, BiFunction<A, B, C> zipper) {
        final Iterator<A> iteratorA = streamA.iterator();
        final Iterator<B> iteratorB = streamB.iterator();
        final Iterator<C> iteratorC = new Iterator<C>() {
            @Override
            public boolean hasNext() {
                return iteratorA.hasNext() && iteratorB.hasNext();
            }

            @Override
            public C next() {
                return zipper.apply(iteratorA.next(), iteratorB.next());
            }
        };
        final boolean parallel = streamA.isParallel() || streamB.isParallel();
        return iteratorToFiniteStream(iteratorC, parallel);
    }

    // borrowing someone's solution boldly right here.
    public static <T> Stream<Tuple2<Integer, T>> zipWithIndex(Stream<T> stream) {

        Stream<Integer> integerStream = IntStream.range(0, Integer.MAX_VALUE).boxed();

        Iterator<Integer> integerIterator = integerStream.iterator();

        return stream.filter((T t) -> t != null)
                     .map((T t) -> new Tuple2<>(integerIterator.next(), t));
    }

    // XXX explore the possibility of creating/having ThrowingFunction<T, U, ? extends Exception> asThrowing(Function<T, U> function)

}
