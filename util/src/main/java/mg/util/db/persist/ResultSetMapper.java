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
import mg.util.functional.predicate.ThrowingPredicate;

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

            T newType = buildNewInstanceFrom(resultSet, type);

            int row = resultSet.getRow();

            buildAndAssignRefsCascading(resultSet, newType);

            resultSet.absolute(row);

            results.add(newType);
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
     */
    public T mapOne(ResultSet resultSet) throws SQLException, ResultSetMapperException, DBValidityException {

        validateResultSet(resultSet);

        T newType = null;

        // TOIMPROVE: consider moving the side effect and resultSet.next() usage outside of this method.
        if (resultSet.next()) {

            newType = buildNewInstanceFrom(resultSet, type);

            buildAndAssignRefsCascading(resultSet, newType);

        } else {

            newType = newInstance(type);
        }

        return newType;
    }

    public T partialMap(ResultSet resultSet) {
        throw new NotYetImplementedException("ResultSetMapper.partialMap has not been implemented yet.");
    }

    // TOIMPROVE: add OneToOne refs handling
    private void buildAndAssignRefsCascading(ResultSet resultSet, T type) throws DBValidityException {

        List<Persistable> uniqueRefs = sqlBuilder.getReferenceCollectionPersistables()
                                                 .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p))
                                                 .values()
                                                 .stream()
                                                 .collect(Collectors.toList());

        List<FieldBuilder> collectionBuilders = sqlBuilder.getCollectionBuilders();
        SqlBuilder typeBuilder = SqlBuilder.of(type);

        uniqueRefs.stream()
                  .forEach((ThrowingConsumer<Persistable, Exception>) ref -> {

                      resultSet.beforeFirst();

                      mapAndAssignReferences(type,
                                             collectionBuilders,
                                             ref,
                                             typeBuilder,
                                             resultSet);
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

    private List<Persistable> filterByReferenceValues(SqlBuilder typeBuilder, List<Persistable> mappedForRef) {
        return mappedForRef.stream()
                           .filter((ThrowingPredicate<Persistable, Exception>) mappedPersistable -> {

                               List<FieldReference> fieldRefs = sqlBuilder.getReferences(typeBuilder, SqlBuilder.of(mappedPersistable));

                               return fieldRefs.stream()
                                               .allMatch(fr -> fr.fieldValuesMatch());

                           })
                           .collect(Collectors.toList());
    }

    private boolean isRefTypeSameAsCollectionElementType(Persistable ref, FieldBuilder colBuilder) {
        Collection<?> col = (Collection<?>) colBuilder.getValue();
        if (!col.isEmpty() &&
            col.iterator().next().getClass().equals(ref.getClass())) {

            return true;
        }
        return false;
    }

    private void mapAndAssignReferences(T type,
        List<FieldBuilder> collectionBuilders,
        Persistable ref,
        SqlBuilder typeBuilder,
        ResultSet resultSet) throws DBValidityException, ResultSetMapperException, SQLException {

        SqlBuilder refBuilder = SqlBuilder.of(ref);
        ResultSetMapper<Persistable> refMapper = ResultSetMapper.of(ref, refBuilder);

        List<Persistable> mappedPersistables = refMapper.map(resultSet);

        collectionBuilders.stream()
                          .forEach(colBuilder -> {

                              if (isRefTypeSameAsCollectionElementType(ref, colBuilder)) {

                                  List<Persistable> filteredAndMappedPersistables = filterByReferenceValues(typeBuilder, mappedPersistables);

                                  colBuilder.setFieldValue(type, filteredAndMappedPersistables);
                              }
                          });
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
