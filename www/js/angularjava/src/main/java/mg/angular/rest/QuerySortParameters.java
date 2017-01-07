package mg.angular.rest;

import static mg.util.Common.hasContent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mg.util.rest.QueryParameterFactory;
import mg.util.rest.QuerySortParameter;

public class QuerySortParameters {

    private List<QuerySortParameter> querySortParams;

    /**
     * Provided for the Jersey String to QuerySortParameter parsing
     */
    public QuerySortParameters(String parameterString) {

        querySortParams = parseQuerySortParams(parameterString);
    }

    public List<QuerySortParameter> getQuerySortParams() {
        return querySortParams;
    }

    private List<QuerySortParameter> parseQuerySortParams(String parameterString) {

        List<QuerySortParameter> querySortParameters = null;

        // TOIMPROVE: failing sort providing proper parameters should result in BAD_REQUEST
        if (!hasContent(parameterString)) {

            querySortParameters = Collections.emptyList();
        } else {

            // String sort = "-id,+name";
            String[] splitParameters = parameterString.split(",");
            if (hasContent(splitParameters)) {

                querySortParameters = Arrays.stream(splitParameters)
                                            .map(QueryParameterFactory::of)
                                            .collect(Collectors.toList());
            }
        }
        return querySortParameters;
    }
}
