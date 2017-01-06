package mg.util.rest;

import static mg.util.Common.hasContent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RestUtil {

    public static List<QuerySortParameter> parseQuerySortParams(String parameterString) {

        List<QuerySortParameter> querySortParameters = Collections.emptyList();

        // TOIMPROVE: failing sort parametering should be covered to BAD_REQUEST
        if (!hasContent(parameterString)) {
            return querySortParameters;
        }

        // String sort = "-id,+name";
        String[] splitParameters = parameterString.split(",");
        if (hasContent(splitParameters)) {

            querySortParameters = Arrays.stream(splitParameters)
                                    .map(QueryParameterFactory::of)
                                    .collect(Collectors.toList());
        }

        return querySortParameters;
    }

}
