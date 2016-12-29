package mg.util.rest;

public class QuerySortParameter {

    private String parameter = "";
    private QueryParameterType type = QueryParameterType.SORT_ASCENDING;

    public QuerySortParameter(String parameter) {
        this.parameter = parameter;
    }

    public QuerySortParameter(String parameter, QueryParameterType type) {
        this.parameter = parameter;
        this.type = type;
    }

    public String getParameter() {
        return parameter;
    }

    public QueryParameterType getType() {
        return type;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public void setType(QueryParameterType queryParameterType) {
        this.type = queryParameterType;
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
