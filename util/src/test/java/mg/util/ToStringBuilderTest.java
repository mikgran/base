package mg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ToStringBuilderTest {

    @Test
    public void test() {

        TestClass testClass = new TestClass();
        String expectedToString = "TestClass('value1', 'value2', '3')";
        assertEquals(expectedToString, testClass.toString());
    }

    private class TestClass {

        public String field1 = "value1";
        public String field2 = "value2";
        public int field3 = 3;

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                                  .add(t -> t.field1)
                                  .add(t -> t.field2)
                                  .add(t -> "" + t.field3)
                                  .build();
        }
    }

}
