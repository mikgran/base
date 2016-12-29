package mg.util.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class QueryParameterFactoryTest {

    @Test
    public void testFactory() {

        String idStringDescending = "-id";
        String nameStringAscending = "+name";

        QuerySortParameter expectedParameter = new QuerySortParameter("id", QueryParameterType.SORT_DESCENDING);
        QuerySortParameter queryParameter = QueryParameterFactory.of(idStringDescending);

        assertNotNull(queryParameter);
        assertParameterEquals("QueryParameter should be: ", expectedParameter, queryParameter);

        expectedParameter = new QuerySortParameter("name", QueryParameterType.SORT_ASCENDING);
        queryParameter = QueryParameterFactory.of(nameStringAscending);

        assertNotNull(queryParameter);
        assertParameterEquals("QueryParameter should be: ", expectedParameter, queryParameter);
    }

    private void assertParameterEquals(String string, QuerySortParameter expected, QuerySortParameter candidate) {
        assertEquals("parameter should be: ", expected.getParameter(), candidate.getParameter());
        assertEquals("type should be: ", expected.getType(), candidate.getType());
    }

}
