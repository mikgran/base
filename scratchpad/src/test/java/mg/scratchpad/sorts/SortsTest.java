package mg.scratchpad.sorts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.scratchpad.sorts.SortObjectComparator.Field;

// this could be SortObjectTest
public class SortsTest {

    private Boolean debug = false;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupOnce() {
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    @Test
    // brainless sorting arrays equals test
    public void testSort1() {

        int[] intArray = {2, 1, 3, 5, 6, 4};
        int[] intArraySorted = {1, 2, 3, 4, 5, 6};

        Arrays.sort(intArray);

        assertTrue("arrays should be equal", Arrays.equals(intArray, intArraySorted));
    }

    @Test
    // brainless sorting arrays equals test
    public void testSort2() {

        SortObject o1 = new SortObject("a", "b", 1);
        SortObject o2 = new SortObject("c", "a", 2);
        SortObject o3 = new SortObject("c", "d", 3);
        SortObject o4 = new SortObject("d", "a", 4);

        SortObject[] array = {o4, o2, o3, o1};

        SortObject p1 = new SortObject("a", "b", 1);
        SortObject p2 = new SortObject("c", "a", 2);
        SortObject p3 = new SortObject("c", "d", 3);
        SortObject p4 = new SortObject("d", "a", 4);

        SortObject[] arraySorted = {p1, p2, p3, p4};

        Arrays.sort(array);

        assertTrue("arrays should be equal", Arrays.equals(array, arraySorted));
    }

    @Test
    // brainless sorting arrays equals test
    public void testSort3() {

        SortObject o1 = new SortObject("a", "b", 1);
        SortObject o2 = new SortObject("c", "a", 2);
        SortObject o3 = new SortObject("c", "d", 3);
        SortObject o4 = new SortObject("d", "a", 4);

        SortObject[] array = {o4, o2, o3, o1};

        SortObject p1 = new SortObject("a", "b", 1);
        SortObject p2 = new SortObject("c", "a", 2);
        SortObject p3 = new SortObject("c", "d", 3);
        SortObject p4 = new SortObject("d", "a", 4);

        SortObject[] arraySorted = {p1, p2, p3, p4};

        Arrays.sort(array, new SortObjectComparator(Field.A));

        printDebug("\nSorted by Field.A", "array:", array);
        printDebug("arraySorted:", arraySorted);

        assertTrue("arrays should be equal", Arrays.equals(array, arraySorted));
    }

    @Test
    // brainless sorting arrays equals test
    public void testSort4() {

        SortObject o1 = new SortObject("a", "b", 1);
        SortObject o2 = new SortObject("c", "a", 2);
        SortObject o3 = new SortObject("c", "d", 3);
        SortObject o4 = new SortObject("d", "a", 4);

        SortObject[] array = {o4, o2, o3, o1};

        SortObject p1 = new SortObject("d", "a", 4);
        SortObject p2 = new SortObject("c", "a", 2);
        SortObject p3 = new SortObject("a", "b", 1);
        SortObject p4 = new SortObject("c", "d", 3);

        Arrays.sort(array, new SortObjectComparator(Field.B));

        SortObject[] arraySorted = {p1, p2, p3, p4};

        printDebug("\nSorted by Field.B", "array:", array);
        printDebug("arraySorted:", arraySorted);

        assertTrue("arrays should be equal", Arrays.equals(array, arraySorted));
    }

    @Test
    // brainless sorting arrays equals test
    public void testSort5() {

        SortObject o1 = new SortObject("a", "b", 1);
        SortObject o2 = new SortObject("c", "a", 2);
        SortObject o3 = new SortObject("c", "d", 3);
        SortObject o4 = new SortObject("d", "a", 4);

        SortObject[] array = {o4, o2, o3, o1};

        SortObject p1 = new SortObject("a", "b", 1);
        SortObject p2 = new SortObject("c", "a", 2);
        SortObject p3 = new SortObject("c", "d", 3);
        SortObject p4 = new SortObject("d", "a", 4);

        Arrays.sort(array, new SortObjectComparator(Field.C));

        SortObject[] arraySorted = {p1, p2, p3, p4};

        printDebug("\nSorted by Field.C", "array:", array);
        printDebug("arraySorted:", arraySorted);

        assertTrue("arrays should be equal", Arrays.equals(array, arraySorted));
    }

    @Test
    // brainless sorting arrays equals test
    public void testSort6() {

        SortObject o1 = new SortObject("a", "a", 1); // by Field.B
        SortObject o2 = new SortObject("a", "b", 2); // by Field.B
        SortObject o3 = new SortObject("c", "a", 3); // by Field.C
        SortObject o4 = new SortObject("c", "a", 4); // by Field.C

        SortObject[] array = {o4, o2, o3, o1};

        printDebug("\nSorted by Fields B and C", "array (unsorted):", array); // by SortObject.equals()

        SortObject p1 = new SortObject("a", "a", 1); // by Field.B
        SortObject p2 = new SortObject("a", "b", 2); // by Field.B
        SortObject p3 = new SortObject("c", "a", 3); // by Field.C
        SortObject p4 = new SortObject("c", "a", 4); // by Field.C

        SortObject[] arraySorted = {p1, p2, p3, p4};

        Arrays.sort(array);

        printDebug("array (after sort):", array);
        printDebug("arraySorted:", arraySorted);

        assertTrue("arrays should be equal", Arrays.equals(array, arraySorted));
    }

    @Test
    // brainless sorting arrays equals test
    public void testSort7f() {

        SortObject o1 = new SortObject("a", "a", 1); // by Field.B
        SortObject o2 = new SortObject("a", "b", 2); // by Field.B
        SortObject o3 = new SortObject("c", "a", 3); // by Field.C
        SortObject o4 = new SortObject("c", "a", 4); // by Field.C

        List<SortObject> theList = Arrays.asList(o4, o2, o3, o1);

        printDebug(theList);

        SortObject p1 = new SortObject("a", "a", 1); // by Field.B
        SortObject p2 = new SortObject("a", "b", 2); // by Field.B
        SortObject p3 = new SortObject("c", "a", 3); // by Field.C
        SortObject p4 = new SortObject("c", "a", 4); // by Field.C

        List<SortObject> expectedList = Arrays.asList(p1, p2, p3, p4);

        Collections.sort(theList, (SortObject a, SortObject b) -> a.compareTo(b));

        printDebug(theList, expectedList);

        assertEquals("the lists should be equal", expectedList, theList);
    }

    private void printDebug(List<SortObject> theList, List<SortObject> expectedList) {
        if (debug) {
            System.out.println("the list after sorting: ");
            theList.stream().forEach(System.out::println);
            System.out.println("the expected list: ");
            expectedList.stream().forEach(System.out::println);
        }
    }

    private void printDebug(List<SortObject> theList) {
        if (debug) {
            System.out.println("the list before sorting: ");
            theList.stream().forEach(System.out::println);
        }
    }

    private void printDebug(String msg1, String msg2, SortObject[] array) {
        if (debug) {
            System.out.println(msg1);
            printDebug(msg2, array);
        }
    }

    private void printDebug(String msg, SortObject[] array) {
        if (debug) {
            System.out.println(msg);
            Arrays.stream(array)
                  .forEach(a -> System.out.println(" " + a));
        }
    }

}
