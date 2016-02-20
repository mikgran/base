package mg.util.db.persist;

public class ResultSetMapperFactory {

    public static <T extends Persistable> ResultSetMapper<T> of(T refType, SqlBuilder sqlBuilder) {
        return new ResultSetMapper<T>(refType, sqlBuilder);
    }

    public static <T extends Persistable> ResultSetMapper<T> of(T refType, SqlBuilder sqlBuilder, DB db) {

        if (FetchPolicy.EAGER.equals(db.getFetchPolicy())) {

            return new ResultSetMapper<T>(refType, sqlBuilder, db);
        } else {
            return new ResultSetLazyMapper<T>(refType, sqlBuilder, db);
        }
    }
}
