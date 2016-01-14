package mg.util.db.persist;

import static mg.util.validation.Validator.validateNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mg.util.NotYetImplementedException;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.functional.consumer.ThrowingConsumer;

public class ResultSetMapper<T extends Persistable> {

    public static <T extends Persistable> ResultSetMapper<T> of(T t, SqlBuilder sqlBuilder) {
        return new ResultSetMapper<T>(t, sqlBuilder);
    }

    private boolean mappingJoinQuery = false;
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

        // TOIMPROVE: consider moving the side effect and resultSet.next() usage outside of this method.
        while (resultSet.next()) {

            T t = mapResultSet(resultSet);

            results.add(t);
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

            t = mapResultSet(resultSet);

        } else {

            t = newInstance();
        }

        return t;
    }

    public T partialMap(ResultSet resultSet) {
        throw new NotYetImplementedException("ResultSetMapper.partialMap has not been implemented yet.");
    }

    public void setMappingJoinQuery(boolean mappingJoinQuery) {
        this.mappingJoinQuery = mappingJoinQuery;
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

        // case: load eager, all fields present in the resultset
        // case: load lazy, only ids for joined tables present in the result set.
        /**
        collectionBuilders.stream()
                          .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                          .filter(object -> object instanceof Persistable)
                          .map(object -> (Persistable) object)
                          .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p)) // this here uses the key as Object.class -> no duplicates
                          .values();

        T t = newInstance();

        List<FieldBuilder> fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                                                 .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                                                 .filter(fieldBuilder -> fieldBuilder.isDbField())
                                                 .collect(Collectors.toList());
        try {
            fieldBuilders.forEach((ThrowingConsumer<FieldBuilder, Exception>) fieldBuilder -> {

                fieldBuilder.setFieldValue(resultSet.getObject(fieldBuilder.getName()));
            });

            t.setFetched(true);

        } catch (RuntimeException e) {
            throw new ResultSetMapperException(e.getCause());
        }

        return t;
         */

        T t = newInstance();

        // TODO: buildNewInstanceFromCascading: join fields mapping and building new instances -> lazy loading and eager loading

        return t;
    }

    private T mapResultSet(ResultSet resultSet) throws ResultSetMapperException, SQLException {
        T t;
        if (mappingJoinQuery) {
            t = buildNewInstanceFromCascading(resultSet);
        } else {
            t = buildNewInstanceFrom(resultSet);
        }
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
