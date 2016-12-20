package mg.util.rest;

import java.util.regex.Pattern;

public class QueryParameterFactory {

    public static final String COMMON_SORT_CHARS = "-+";
    public static final String COMMON_SORT_CHARS_REGEX = "[" + Pattern.quote(COMMON_SORT_CHARS) + "]";

    public static QueryParameter of(String queryParameterString) {

        QueryParameterType type = determineTypeOf(queryParameterString);

        // TOIMPROVE: -name-something, find out the prefix and leave the chars in the middle alone.
        // TOCONSIDER: allow post fixes too.
        queryParameterString = queryParameterString.replaceAll(COMMON_SORT_CHARS_REGEX, "");

        return new QueryParameter(queryParameterString, type);
    }

    private static QueryParameterType determineTypeOf(String queryParameterString) {

        if (queryParameterString.startsWith("-")) {
            return QueryParameterType.DESCENDING;
        }

        // by default if no prefix present, assume ascending sort ordering for the named field
        return QueryParameterType.ASCENDING;
    }

}
