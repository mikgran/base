package mg.util.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mg.util.Common;
import mg.util.Tuple2;

public class RestUtilTest {

    @Test
    public void testQueryParamExtract1() {

        // XXX: LAST: parameter sorting and validating towards fields

        // GET .../contacts?sort=-id,+name
        // public void getContacts(DefaultValue("") QueryParam("sort") String sort)
        String sort = "-id,+name";
        ArrayList<QuerySortParameter> expectedList = new ArrayList<>();
        expectedList.add(new QuerySortParameter("id", QueryParameterType.SORT_DESCENDING));
        expectedList.add(new QuerySortParameter("name", QueryParameterType.SORT_ASCENDING));

        List<QuerySortParameter> parsedParameters = RestUtil.parseQuerySortParams(sort);

        assertNotNull(parsedParameters);
        assertEquals("there should be parameters: ", 2, parsedParameters.size());
        assertQueryParameterEquals(expectedList, parsedParameters);
    }

    private void assertQueryParameterEquals(ArrayList<QuerySortParameter> expectedParameters, List<QuerySortParameter> parsedParameters) {

        if (expectedParameters.size() != parsedParameters.size()) {
            fail("lists differ in size, expected: " + expectedParameters + ", got: " + parsedParameters);
        }

        Common.zip(expectedParameters, parsedParameters, (a, b) -> new Tuple2<>(a, b))
              .forEach(tuple -> {

                  assertEquals("QueryParameter should be: ", tuple._1.getParameter(), tuple._2.getParameter());
              });

    }

}
