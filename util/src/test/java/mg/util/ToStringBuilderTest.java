package mg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(expectedToString, builtToString);
    }

    @Test
    public void testPadderFunction() {

        ToStringBuilder.reflectiveOf(new TestClass())
                       .build();
    }

    private class TestClass {
        public String field1 = "value1";
        public String field2 = "value2";
        public int field3 = 3;
    }
}
