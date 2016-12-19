package mg.util.rest;

import static mg.util.Common.hasContent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RestUtil {

    public static final String COMMON_SORT_CHARS = "<>-+";
    // public static final String COMMON_SORT_CHARS_REGEX = "[" + Pattern.quote(COMMON_SORT_CHARS) + "]";

    public static List<QueryParameter> parseQueryParams(String parameterString) {

        List<QueryParameter> queryParameters = Collections.emptyList();

        // parameterString = parameterString.replaceAll(COMMON_SORT_CHARS_REGEX, "");

        // String sort = "-id,+name";
        String[] splitParameters = parameterString.split(",");
        if (hasContent(splitParameters)) {

            queryParameters = Arrays.stream(splitParameters)
                                    .map(s -> new QueryParameter(s))
                                    .collect(Collectors.toList());
        }

        return queryParameters;
    }

}
