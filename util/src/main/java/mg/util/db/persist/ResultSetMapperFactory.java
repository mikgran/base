package mg.util.db.persist;

public class ResultSetMapperFactory {

    public static <T extends Persistable> ResultSetMapper<T> of(T refType, SqlBuilder sqlBuilder) {
        return new ResultSetMapper<T>(refType, sqlBuilder);
    }

}
