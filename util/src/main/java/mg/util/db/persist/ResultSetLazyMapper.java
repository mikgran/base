package mg.util.db.persist;

import java.sql.ResultSet;

public class ResultSetLazyMapper<T extends Persistable> extends ResultSetMapper<T> {

    public ResultSetLazyMapper(T refType, SqlBuilder sqlBuilder) {
        super(refType, sqlBuilder);
    }

    @SuppressWarnings("unused")
    private void assignProxies(ResultSet resultSet, T newType, T refType2) throws DBValidityException {

        SqlBuilder newTypeBuilder = SqlBuilderFactory.of(newType);

        // mapOneToManyAndAssignByMatchingReferenceValues(resultSet, newType, refType, newTypeBuilder);
        //
        // mapOneToOneAndAssignByMatchingReferenceValues(resultSet, newType, refType, newTypeBuilder);
        // 1. assign proxy lists with parameters (including fetch by sql)
        // 2. on: get, size, iterator, foreach.. etc -> fetch
        // 3. fetch one-to-ones one level only -> partial joins
    }
}
