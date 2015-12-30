package mg.scratchpad.functional;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FunctionalTest {

    @BeforeClass
    public static void setupOnce() {
    }

    @AfterClass
    public static void tearDownOnce() {
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFoo() {

        // brainless testing of flatmap and map for nested looping example
        List<String> x = Arrays.asList("1", "2", "3", "4");
        List<String> y = Arrays.asList("a", "b", "c");
        List<String> expectedStrings = Arrays.asList("1 a", "1 b", "1 c", "2 a", "2 b", "2 c", "3 a", "3 b", "3 c", "4 a", "4 b", "4 c");
        List<String> joinedStrings = x.stream().flatMap(a -> y.stream().map(b -> (a + " " + b))).collect(Collectors.toList());
        assertEquals(expectedStrings, joinedStrings);
    }

}