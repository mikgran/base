package mg.util.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mg.util.Common;
import mg.util.Tuple2;
import mg.util.db.persist.support.Contact;

public class RestUtilTest {

    @Test
    public void testQueryParamExtract1() {

        // XXX: LAST: parameter sorting and validating towards fields

        // GET .../contacts?sort=-id,+name
        // public void getContacts(DefaultValue("") QueryParam("sort") String sort)
        String sort = "-id,+name";
        ArrayList<QueryParameter> expectedList = new ArrayList<>();
        expectedList.add(new QueryParameter("id", QueryParameterType.DESCENDING));
        expectedList.add(new QueryParameter("name", QueryParameterType.ASCENDING));

        Contact contact = new Contact(1L, "Test Testey", "test.testey@mail.com", "123 4567");

        List<QueryParameter> parsedParameters = RestUtil.parseQueryParams(sort);

        assertNotNull(parsedParameters);
        assertEquals("there should be parameters: ", 2, parsedParameters.size());
        assertQueryParameterEquals(expectedList, parsedParameters);


    }

    private void assertQueryParameterEquals(ArrayList<QueryParameter> expectedParameters, List<QueryParameter> parsedParameters) {

        if (expectedParameters.size() != parsedParameters.size()) {
            fail("lists differ in size, expected: " + expectedParameters + ", got: " + parsedParameters);
        }

        Common.zip(expectedParameters, parsedParameters, (a, b) -> new Tuple2<>(a, b))
              .forEach(tuple -> {

                  assertEquals("QueryParameter should be: ", tuple._1.getParameter(), tuple._2.getParameter());
              });

    }

    private ArrayList<QueryParameter> getExpectedQueryParametersList1() {
        ArrayList<QueryParameter> expectedList = new ArrayList<>();
        expectedList.add(new QueryParameter("id", QueryParameterType.DESCENDING));
        expectedList.add(new QueryParameter("name", QueryParameterType.ASCENDING));
        return expectedList;
    }

}
