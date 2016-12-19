package mg.util.rest;

public class QueryParameterFactory {

    public static QueryParameter of(String queryParameterString) {

        String s = null;
        QueryParameterType type = null;

        determineTypeOf(queryParameterString);

        new QueryParameter(s, type);




        return null;
    }

    private static QueryParameterType determineTypeOf(String queryParameterString) {

        if (queryParameterString.contains("-")) {
            return QueryParameterType.DESCENDING;
        }

        // by default if no prefix present, assume ascending sort ordering for the named field
        return QueryParameterType.ASCENDING;
    }

}
