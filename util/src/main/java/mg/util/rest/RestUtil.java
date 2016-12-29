package mg.util.rest;

import static mg.util.Common.hasContent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RestUtil {

    public static List<QueryParameter> parseQueryParams(String parameterString) {

        List<QueryParameter> queryParameters = Collections.emptyList();

        if (!hasContent(parameterString)) {
            return queryParameters;
        }

        // String sort = "-id,+name";
        String[] splitParameters = parameterString.split(",");
        if (hasContent(splitParameters)) {

            queryParameters = Arrays.stream(splitParameters)
                                    .map(QueryParameterFactory::of)
                                    .collect(Collectors.toList());
        }

        return queryParameters;
    }

}
