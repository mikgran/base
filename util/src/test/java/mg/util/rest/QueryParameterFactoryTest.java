package mg.util.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class QueryParameterFactoryTest {

    @Test
    public void testFactory() {

        //
        String string = "-id";
        String string2 = "+name";
        QueryParameter queryParameter = QueryParameterFactory.of(string);
        QueryParameter expectedParameter = new QueryParameter("id", QueryParameterType.DESCENDING);

        assertNotNull(queryParameter);
        assertEquals("QueryParameter should be: ", expectedParameter, queryParameter);
    }

}
