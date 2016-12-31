package mg.util.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ColumnPrinterTest {

    @Test
    public void testPrintingVariableLength() {

        String expectedLine = "persons.firstName persons.lastName\n" +
                              "1                 2               \n";

        ColumnPrinter columnPrinter = new ColumnPrinter();

        columnPrinter.addHeader("persons.firstName");
        columnPrinter.addHeader("persons.lastName");
        columnPrinter.add("1");
        columnPrinter.add("2");
        // columnPrinter.delimeter(", ");
        String candidateLine = columnPrinter.toString();

        assertNotNull(candidateLine);
        assertEquals("variable length line should equal to: ", expectedLine, candidateLine);
    }

    @Test
    public void testPrintingVariableLength2() {

        String expectedLine = "persons.firstName, persons.lastName, persons.phone\n" +
                              "               A1,               A2,           111\n" +
                              "               B1,               B2,           222\n";

        ColumnPrinter columnPrinter = new ColumnPrinter();

        columnPrinter.addHeader("persons.firstName");
        columnPrinter.addHeader("persons.lastName");
        columnPrinter.addHeader("persons.phone");
        columnPrinter.add("A1");
        columnPrinter.add("A2");
        columnPrinter.add("111");
        columnPrinter.add("B1");
        columnPrinter.add("B2");
        columnPrinter.add("222");
        columnPrinter.delimiter(", ");
        columnPrinter.padRight();

        String candidateLine = columnPrinter.toString();

        assertNotNull(candidateLine);
        assertEquals("variable length line should equal to: ", expectedLine, candidateLine);
    }

    @Test
    public void testPrintingVariableLength3() {

        String expectedLine = "persons.firstName     persons.lastName\n" +
                              "111111111111111111111 2               \n" +
                              "33333333333333333     4               \n";

        ColumnPrinter columnPrinter = new ColumnPrinter();

        columnPrinter.addHeader("persons.firstName");
        columnPrinter.addHeader("persons.lastName");
        columnPrinter.add("111111111111111111111");
        columnPrinter.add("2");
        columnPrinter.add("33333333333333333");
        columnPrinter.add("4");

        String candidateLine = columnPrinter.toString();

        assertNotNull(candidateLine);
        assertEquals("variable length line should equal to: ", expectedLine, candidateLine);
    }

}
