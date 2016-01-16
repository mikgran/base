package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import mg.util.NotYetImplementedException;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.functional.consumer.ThrowingConsumer;

public class ResultSetMapper<T extends Persistable> {

    public enum MappingPolicy {
        EAGER, LAZY;
    }

    public static <T extends Persistable> ResultSetMapper<T> of(T t, SqlBuilder sqlBuilder) {
        return new ResultSetMapper<T>(t, sqlBuilder);
    }

    private boolean isMappingJoinQuery = false;
    private MappingPolicy mappingPolicy = MappingPolicy.EAGER;
    private SqlBuilder sqlBuilder;
    private T type;

    /**
     * Constructs the ResultSetMapper.
     * @param type The object to use in instantiation with reflection type.newInstance();
     */
    public ResultSetMapper(T type, SqlBuilder sqlBuilder) {
        this.sqlBuilder = validateNotNull("sqlBuilder", sqlBuilder);
        this.type = validateNotNull("type", type);
    }

    public List<T> map(ResultSet resultSet) throws SQLException, ResultSetMapperException {

        validateResultSet(resultSet);

        List<T> results = new ArrayList<T>();

        // case: load eager, all fields present in the resultset
        // case: load lazy, only ids for joined tables present in the result set.
        // TODO: map: join fields mapping and building new instances -> lazy loading and eager loading
        // TODO: map: map && mapOne implementation for join queries
        // assumed contents of join query:
        // p1.firstName, p1.id, p1.lastName, t1.id, t1.personId, t1.todo
        // a             1      b            1      1            a-to-do1
        // a             1      b            2      1            a-to-do2
        // a             1      b            3      1            a-to-do3
        System.out.println();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            System.out.print(metaData.getColumnLabel(i) + " ");
        }
        System.out.println();

        while (resultSet.next()) {
            for (int j = 1; j <= columnCount; j++) {
                System.out.print(resultSet.getString(j) + " ");
            }
        }

        if (isMappingJoinQuery) {

            RowSetFactory rowSetFactory = RowSetProvider.newFactory();
            CachedRowSet cachedRowSet = rowSetFactory.createCachedRowSet();
            cachedRowSet.populate(resultSet);
            // cachedRowSet.

            while (resultSet.next()) {

                T t = buildNewInstanceFrom(resultSet);

                results.add(t);
            }

        } else {

            while (resultSet.next()) {

                T t = buildNewInstanceFrom(resultSet);

                results.add(t);
            }

        }

        return results;
    }

    /**
     * Maps resultSet first row to a {@code <T extends Persistable>}.
     * Any remaining rows in the ResultSet are ignored. If the ResultSet contains
     * multiple rows, the first row matching type T will be mapped and returned.
     * <br><br>
     * This method expects a ResultSet with all the columns. Use
     * partialMap to partially map the columns in the ResultSet. Any mismatch between
     * selected columns and the type T object will result in ResultSetMapperException.
     * <br><br>
     * The first item using default sort order is used unless another sorting method is
     * used.
     *
     * @param resultSet the ResultSet containing at least one row of data corresponding the type T Persistable.
     * @return a type T object created by retrieving the data from the provided resultSet. If
     * no rows are present in the resultSet, an empty object of type T is returned.
     * @throws SQLException on any database related exception.
     * @throws ResultSetMapperException If mapping fails or the resultSet was closed.
     */
    public T mapOne(ResultSet resultSet) throws SQLException, ResultSetMapperException {

        validateResultSet(resultSet);

        T t = null;

        // TOIMPROVE: consider moving the side effect and resultSet.next() usage outside of this method.
        if (resultSet.next()) {

            t = buildNewInstanceFrom(resultSet);

        } else {

            t = newInstance();
        }

        return t;
    }

    public T partialMap(ResultSet resultSet) {
        throw new NotYetImplementedException("ResultSetMapper.partialMap has not been implemented yet.");
    }

    public void setIsMappingJoinQuery(boolean isMappingJoinQuery) {
        this.isMappingJoinQuery = isMappingJoinQuery;
    }

    private T buildNewInstanceFrom(CachedRowSet cachedRowSet) throws ResultSetMapperException, SQLException {

        T newType = newInstance();

        AliasBuilder aliasBuilder = sqlBuilder.getAliasBuilder();
        String tableName = sqlBuilder.getTableName();
        String tableNameAlias = aliasBuilder.aliasOf(tableName);
        List<FieldBuilder> fieldBuilders = sqlBuilder.getFieldBuilders();

        try {
            fieldBuilders.forEach((ThrowingConsumer<FieldBuilder, Exception>) fieldBuilder -> {

                fieldBuilder.setFieldValue(newType, cachedRowSet.getObject(tableNameAlias + "." + fieldBuilder.getName()));
            });

            newType.setFetched(true);

        } catch (RuntimeException e) {
            throw new ResultSetMapperException(e.getCause());
        }

        return newType;
    }

    private T buildNewInstanceFrom(ResultSet resultSet) throws ResultSetMapperException, SQLException {

        T newType = newInstance();

        AliasBuilder aliasBuilder = sqlBuilder.getAliasBuilder();
        String tableName = sqlBuilder.getTableName();
        String tableNameAlias = aliasBuilder.aliasOf(tableName);
        List<FieldBuilder> fieldBuilders = sqlBuilder.getFieldBuilders();

        try {
            fieldBuilders.forEach((ThrowingConsumer<FieldBuilder, Exception>) fieldBuilder -> {

                fieldBuilder.setFieldValue(newType, resultSet.getObject(tableNameAlias + "." + fieldBuilder.getName()));
            });

            newType.setFetched(true);

        } catch (RuntimeException e) {
            throw new ResultSetMapperException(e.getCause());
        }

        return newType;
    }

    private T buildNewInstanceFromCascading(ResultSet resultSet) throws ResultSetMapperException {

        T t = newInstance();

        return t;
    }

    @SuppressWarnings("unchecked")
    private T newInstance() throws ResultSetMapperException {

        try {
            return (T) type.getClass().newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResultSetMapperException("Exception in instantiating type T: " + e.getMessage());
        }
    }

    private void validateResultSet(ResultSet resultSet) throws SQLException, ResultSetMapperException {
        validateNotNull("resultSet", resultSet);

        if (resultSet.isClosed()) {
            throw new ResultSetMapperException("ResultSet can not be closed.");
        }
    }

}
