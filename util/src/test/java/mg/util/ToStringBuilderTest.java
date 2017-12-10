package mg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ToStringBuilderTest {

    @Test
    public void testBuild() {

        String builtToString = ToStringBuilder.of(new TestClass())
                                              .add(t -> t.field1)
                                              .add(t -> t.field2)
                                              .add(t -> "" + t.field3)
                                              .build();

        String expectedToString = "TestClass(value1, value2, 3)";
        assertNotNull(builtToString);
        assertEquals(expectedToString, builtToString);
    }

    @Test
    public void testReflectiveBuild() {
        String builtToString = ToStringBuilder.reflectiveOf(new TestClass())
                                              .build();

        String expectedToString = "TestClass(field1: 'value1', field2: 'value2', field3: '3')";
        assertNotNull(builtToString);
        assertEquals(expectedToString, builtToString);
    }


}
