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

        // GET .../contacts?sort=-id,+name
        // public void getContacts(DefaultValue("") QueryParam("sort") String sort)
        String sort = "-id,+name";
        ArrayList<QuerySortParameter> expectedList = new ArrayList<>();
        expectedList.add(new QuerySortParameter("id", QueryParameterType.SORT_DESCENDING));
        expectedList.add(new QuerySortParameter("name", QueryParameterType.SORT_ASCENDING));

        List<QuerySortParameter> parameterCandidates = RestUtil.parseQuerySortParams(sort);

        assertNotNull(parameterCandidates);
        assertEquals("there should be parameters: ", 2, parameterCandidates.size());
        assertQueryParameterEquals(expectedList, parameterCandidates);
    }

    private void assertQueryParameterEquals(ArrayList<QuerySortParameter> expectedParameters, List<QuerySortParameter> parameterCandidates) {

        if (expectedParameters.size() != parameterCandidates.size()) {
            fail("lists differ in size, expected: " + expectedParameters + ", got: " + parameterCandidates);
        }

        Common.zip(expectedParameters, parameterCandidates, (a, b) -> new Tuple2<>(a, b))
              .forEach(tuple -> {

                  assertEquals("QuerySortParameter should be: ", tuple._1.getParameter(), tuple._2.getParameter());
              });

    }

}
