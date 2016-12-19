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
        // public void getContacts(DefaultValue("") QueryParam("sort") String sort)
        String sort = "-id,+name";

        ArrayList<QueryParameter> expectedList = getExpectedQueryParametersList1();

        // GET .../contacts?sort=-id,+name
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

                  assertEquals("QueryParameter should be: ", tuple._1.getS(), tuple._2.getS());
              });

    }

    private ArrayList<QueryParameter> getExpectedQueryParametersList1() {
        ArrayList<QueryParameter> expectedList = new ArrayList<>();
        expectedList.add(new QueryParameter("-id"));
        expectedList.add(new QueryParameter("+name"));
        return expectedList;
    }

}
