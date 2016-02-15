package mg.util.db.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetLazyMapper<T extends Persistable> extends ResultSetMapper<T> {

    public ResultSetLazyMapper(T refType, SqlBuilder sqlBuilder) {
        super(refType, sqlBuilder);
    }

    public T mapOne(ResultSet resultSet) throws SQLException, DBMappingException, DBValidityException {

        validateResultSet(resultSet);

        T newType = null;

        if (resultSet.next()) {

            newType = buildNewInstanceFrom(resultSet, refType);

            buildAndAssignProxies(resultSet, newType, refType);
        }

        return newType;
    }

    @SuppressWarnings("unused")
    private void buildAndAssignProxies(ResultSet resultSet, T newType, T refType2) throws DBValidityException {

        SqlBuilder newTypeBuilder = SqlBuilderFactory.of(newType);

        // mapOneToManyAndAssignByMatchingReferenceValues(resultSet, newType, refType, newTypeBuilder);
        //
        // mapOneToOneAndAssignByMatchingReferenceValues(resultSet, newType, refType, newTypeBuilder);
        // 1. assign proxy lists with parameters (including fetch by sql)
        // 2. on: get, size, iterator, foreach.. etc -> fetch
        // 3. fetch one-to-ones one level only -> partial joins
    }
}
