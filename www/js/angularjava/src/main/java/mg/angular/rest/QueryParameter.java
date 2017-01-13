package mg.angular.rest;

public class QueryParameter {

    private String parameterString;

    // TOIMPROVE: initial support for whole phrase only, improve with free search against every word and allow negations of words also against all DB columns.
    public QueryParameter(String parameterString) {
        this.parameterString = parameterString != null ? parameterString : "";
    }

    public String getParameterString() {
        return parameterString;
    }
}
