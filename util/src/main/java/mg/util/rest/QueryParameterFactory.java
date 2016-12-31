package mg.util.rest;

import java.util.regex.Pattern;

public class QueryParameterFactory {

    public static final String COMMON_SORT_CHARS = "-+";
    public static final String COMMON_SORT_CHARS_REGEX = "[" + Pattern.quote(COMMON_SORT_CHARS) + "]";

    public static QuerySortParameter of(String queryParameterString) {

        QuerySortParameterType type = determineTypeOf(queryParameterString);

        // TOIMPROVE: -name-something, find out the prefix and leave the chars in the middle alone.
        // TOCONSIDER: allow post fixes too.
        queryParameterString = queryParameterString.replaceAll(COMMON_SORT_CHARS_REGEX, "");

        return new QuerySortParameter(queryParameterString, type);
    }

    private static QuerySortParameterType determineTypeOf(String queryParameterString) {

        if (queryParameterString.startsWith("-")) {
            return QuerySortParameterType.SORT_DESCENDING;
        }

        // by default if no prefix present, assume ascending sort ordering for the named field
        return QuerySortParameterType.SORT_ASCENDING;
    }

}
