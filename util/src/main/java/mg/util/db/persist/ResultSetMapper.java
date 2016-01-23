package mg.util.db.persist;

import static mg.util.Common.unwrapCauseAndRethrow;
import static mg.util.validation.Validator.validateNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    // private MappingPolicy mappingPolicy = MappingPolicy.EAGER;
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

    public List<T> map(ResultSet resultSet) throws DBValidityException, ResultSetMapperException, SQLException {

        validateResultSet(resultSet);

        List<T> results = new ArrayList<T>();

        // case: load eager, all fields present in the resultset
        // case: load lazy, only ids for joined tables present in the result set.
        // TODO: map: join fields mapping and building new instances -> lazy loading and eager loading
        // TODO: map: map && mapOne implementation for join queries

        while (resultSet.next()) {

            T t = buildNewInstanceFrom(resultSet, type);

            int row = resultSet.getRow();

            buildAndAssignRefs(resultSet, t);

            resultSet.absolute(row);

            // if T references type by id fields -> add.

            results.add(t);
        }
        results = removeDuplicatesByPrimaryKey(results);

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

            t = buildNewInstanceFrom(resultSet, type);

        } else {

            t = newInstance(type);
        }

        return t;
    }

    public T partialMap(ResultSet resultSet) {
        throw new NotYetImplementedException("ResultSetMapper.partialMap has not been implemented yet.");
    }

    private void buildAndAssignRefs(ResultSet resultSet, T type) throws DBValidityException {

        List<Persistable> refs = sqlBuilder.getReferencePersistables()
                                           .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p))
                                           .values()
                                           .stream()
                                           .collect(Collectors.toList());

        List<FieldBuilder> collectionBuilders = sqlBuilder.getCollectionBuilders();

        refs.stream()
            .forEach((ThrowingConsumer<Persistable, Exception>) ref -> {

                resultSet.beforeFirst();

                SqlBuilder refBuilder = SqlBuilder.of(ref);
                ResultSetMapper<Persistable> refMapper = ResultSetMapper.of(ref, refBuilder);

                List<Persistable> mappedForRef = refMapper.map(resultSet);

                // TOIMPROVE: add OneToOne refs handling
                collectionBuilders.stream().forEach(colBuilder -> {

                    Collection<?> col = (Collection<?>) colBuilder.getValue();
                    if (!col.isEmpty() &&
                        col.iterator().next().getClass().equals(ref.getClass())) {

                        colBuilder.setFieldValue(type, mappedForRef);
                    }

                });

            });
    }

    private T buildNewInstanceFrom(ResultSet resultSet, T type) throws ResultSetMapperException, SQLException {

        T newType = newInstance(type);

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
            unwrapCauseAndRethrow(e);
        }

        return newType;
    }

    @SuppressWarnings("unchecked")
    private T newInstance(T type) throws ResultSetMapperException {

        try {
            return (T) type.getClass().newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResultSetMapperException("Exception in instantiating type T: " + e.getMessage());
        }
    }

    private List<T> removeDuplicatesByPrimaryKey(List<T> persistables) {

        List<T> results;

        if (persistables != null && persistables.size() > 0) {

            FieldBuilder pkBuilder = sqlBuilder.getPrimaryKeyBuilder();

            Collection<T> uniquePersistables;
            uniquePersistables = persistables.stream()
                                             .collect(Collectors.toMap(t -> pkBuilder.getFieldValue(t, pkBuilder.getDeclaredField()), t -> t, (t, v) -> t))
                                             .values();

            results = new ArrayList<T>(uniquePersistables);

        } else {

            results = persistables;
        }

        return results;
    }

    private void validateResultSet(ResultSet resultSet) throws SQLException, ResultSetMapperException {
        validateNotNull("resultSet", resultSet);

        if (resultSet.isClosed()) {
            throw new ResultSetMapperException("ResultSet can not be closed.");
        }
    }

}
