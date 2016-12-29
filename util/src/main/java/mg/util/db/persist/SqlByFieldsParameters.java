package mg.util.db.persist;

public class SqlByFieldsParameters {

    public final String constraints;
    public final String fieldNames;
    public final String joins;
    public final String tableNameAlias;
    public final String orderings;

    public SqlByFieldsParameters(String fieldNames,  String constraints, String orderings, String tableNameAlias) {
        this.fieldNames = fieldNames;
        this.joins = "";
        this.constraints = constraints;
        this.tableNameAlias = tableNameAlias;
        this.orderings = orderings;
    }

    public SqlByFieldsParameters(String fieldNames, String joins, String constraints, String orderings, String tableNameAlias) {
        this.fieldNames = fieldNames;
        this.joins = joins;
        this.constraints = constraints;
        this.tableNameAlias = tableNameAlias;
        this.orderings = orderings;
    }
}