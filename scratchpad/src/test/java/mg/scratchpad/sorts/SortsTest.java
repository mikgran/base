package mg.scratchpad.sorts;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.scratchpad.sorts.SortObject;
import mg.scratchpad.sorts.SortObjectComparator.Field;

// this could be SortObjectTest
public class SortsTest {

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

        System.out.println("\nSorted by Field.A");
        System.out.println("array:");
        Arrays.stream(array)
            .forEach(a -> System.out.println(" " + a));
        System.out.println("arraySorted:");
        Arrays.stream(arraySorted)
            .forEach(a -> System.out.println(" " + a));

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

        System.out.println("\nSorted by Field.B");
        System.out.println("array:");
        Arrays.stream(array)
            .forEach(a -> System.out.println(" " + a));
        System.out.println("arraySorted:");
        Arrays.stream(arraySorted)
            .forEach(a -> System.out.println(" " + a));

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

        System.out.println("\nSorted by Field.C");
        System.out.println("array:");
        Arrays.stream(array)
            .forEach(a -> System.out.println(" " + a));
        System.out.println("arraySorted:");
        Arrays.stream(arraySorted)
            .forEach(a -> System.out.println(" " + a));

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
        
        System.out.println("\nSorted by Fields B and C");
        System.out.println("array (unsorted):");
        Arrays.stream(array)
            .forEach(a -> System.out.println(" " + a));

        SortObject p1 = new SortObject("a", "a", 1); // by Field.B
        SortObject p2 = new SortObject("a", "b", 2); // by Field.B
        SortObject p3 = new SortObject("c", "a", 3); // by Field.C
        SortObject p4 = new SortObject("c", "a", 4); // by Field.C
        
        SortObject[] arraySorted = {p1, p2, p3, p4};

        Arrays.sort(array);

        System.out.println("array (after sort):");
        Arrays.stream(array)
            .forEach(a -> System.out.println(" " + a));
        System.out.println("arraySorted:");
        Arrays.stream(arraySorted)
            .forEach(a -> System.out.println(" " + a));
        
        assertTrue("arrays should be equal", Arrays.equals(array, arraySorted));
    }

}
