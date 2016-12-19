package mg.util.rest;

public class QueryParameter {

    private QueryParameterType queryParameterType = QueryParameterType.ASCENDING;
    private String s = "";

    public QueryParameter(String s) {
        this.s = s;
    }

    public QueryParameter(String s, QueryParameterType queryParameterType) {
        this.queryParameterType = queryParameterType;
        this.s = s;
    }

    public QueryParameterType getQueryParameterType() {
        return queryParameterType;
    }

    public String getS() {
        return s;
    }

    public void setQueryParameterType(QueryParameterType queryParameterType) {
        this.queryParameterType = queryParameterType;
    }

    public void setS(String s) {
        this.s = s;
    }

}
