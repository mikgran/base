package mg.angular.rest;

import javax.ws.rs.core.MultivaluedMap;

public class ParameterUtil {

    public static QueryParameters parse(MultivaluedMap<String, String> queryParameters) {

        QueryParameters returnValue = new QueryParameters();




        return returnValue;
    }

}
