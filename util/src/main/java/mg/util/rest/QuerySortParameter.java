package mg.util.rest;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

public class QuerySortParameter {

    private String parameter = "";
    private QuerySortParameterType type = QuerySortParameterType.SORT_ASCENDING;

    public QuerySortParameter(String parameter) {
        this.parameter = validateNotNullOrEmpty("parameter", parameter);
        this.parameter = parameter;
        this.type = QuerySortParameterType.SORT_ASCENDING;
    }

    public QuerySortParameter(String parameter, QuerySortParameterType type) {
        this.parameter = validateNotNullOrEmpty("parameter", parameter);
        this.type = validateNotNull("type", type);
    }

    public String getParameter() {
        return parameter;
    }

    public QuerySortParameterType getType() {
        return type;
    }

    @Override
    public String toString() {

        return new StringBuffer().append("QueryParameter(")
                                 .append("parameter: '")
                                 .append(parameter)
                                 .append("', type: '")
                                 .append(type.toString())
                                 .append("'")
                                 .toString();
    }

}
