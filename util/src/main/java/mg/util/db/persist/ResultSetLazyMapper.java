package mg.util.db.persist;

public class ResultSetLazyMapper<T extends Persistable> extends ResultSetMapper<T> {

    public ResultSetLazyMapper(T refType, SqlBuilder sqlBuilder) {
        super(refType, sqlBuilder);
    }

}
