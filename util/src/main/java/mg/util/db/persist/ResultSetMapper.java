package mg.util.db.persist;

import static mg.util.Common.unwrapCauseAndRethrow;
import static mg.util.validation.Validator.validateNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mg.util.NotYetImplementedException;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.functional.predicate.ThrowingPredicate;

public class ResultSetMapper<T extends Persistable> {

    public enum MappingPolicy {
        EAGER, LAZY;
    }

    public static <T extends Persistable> ResultSetMapper<T> of(T refType, SqlBuilder sqlBuilder) {
        return new ResultSetMapper<T>(refType, sqlBuilder);
    }

    // private MappingPolicy mappingPolicy = MappingPolicy.EAGER;
    private SqlBuilder refSqlBuilder;
    private T refType;

    /**
     * Constructs the ResultSetMapper.
     * @param refType The object to use in instantiation with reflection type.newInstance();
     */
    public ResultSetMapper(T refType, SqlBuilder sqlBuilder) {
        this.refSqlBuilder = validateNotNull("sqlBuilder", sqlBuilder);
        this.refType = validateNotNull("refType", refType);
    }

    public List<T> map(ResultSet resultSet) throws DBValidityException, ResultSetMapperException, SQLException {

        validateResultSet(resultSet);

        List<T> results = new ArrayList<T>();

        // case: load eager, all fields present in the resultset
        // case: load lazy, only ids for joined tables present in the result set.
        // TODO: map: lazy loading
        while (resultSet.next()) {

            T newType = buildNewInstanceFrom(resultSet, refType);

            results.add(newType);
        }
        results = removeDuplicatesByPrimaryKey(results);

        results.forEach((ThrowingConsumer<T, Exception>) newType -> {

            buildAndAssignRefsCascading(resultSet, newType, refType);
        });

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

        if (resultSet.next()) {

            newType = buildNewInstanceFrom(resultSet, refType);

            buildAndAssignRefsCascading(resultSet, newType, refType);

        } else {

            newType = newInstance(refType);
        }

        return newType;
    }

    public T partialMap(ResultSet resultSet) {
        throw new NotYetImplementedException("ResultSetMapper.partialMap has not been implemented yet.");
    }

    private void buildAndAssignRefsCascading(ResultSet resultSet, T newType, T refType) throws DBValidityException {

        SqlBuilder newTypeBuilder = SqlBuilder.of(newType);

        mapOneToManyAndAssingByMatchingReferenceValues(resultSet, newType, refType, newTypeBuilder);

        mapOneToOneAndAssignByMatchingReferenceValues(resultSet, newType, newTypeBuilder);
    }

    private T buildNewInstanceFrom(ResultSet resultSet, T type) throws ResultSetMapperException, SQLException {

        T newType = newInstance(type);

        AliasBuilder aliasBuilder = refSqlBuilder.getAliasBuilder();
        String tableName = refSqlBuilder.getTableName();
        String tableNameAlias = aliasBuilder.aliasOf(tableName);
        List<FieldBuilder> fieldBuilders = refSqlBuilder.getFieldBuilders();

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

    private Stream<Persistable> filterByReferenceValues(SqlBuilder typeBuilder, List<Persistable> mappedForRef) throws DBValidityException {
        return mappedForRef.stream()
                           .filter((ThrowingPredicate<Persistable, Exception>) mappedPersistable -> {

                               return refSqlBuilder.getReferences(typeBuilder, SqlBuilder.of(mappedPersistable))
                                                   .allMatch(fieldReference -> fieldReference.fieldValuesMatch());
                           });
    }

    private boolean isMappingTypeSameAsRefType(Persistable typeToMapFor, T refType, FieldBuilder refColBuilder) {
        Collection<?> col = (Collection<?>) refColBuilder.getFieldValue(refType);
        if (!col.isEmpty() &&
            col.iterator().next().getClass().equals(typeToMapFor.getClass())) {

            return true;
        }
        return false;
    }

    private void mapOneToManyAndAssingByMatchingReferenceValues(ResultSet resultSet, T newType, T refType, SqlBuilder newTypeBuilder) throws DBValidityException {

        List<FieldBuilder> oneToManyBuilders = refSqlBuilder.getOneToManyBuilders();
        Collection<Persistable> colMapTypes = refSqlBuilder.getReferenceCollectionPersistables()
                                                           .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p)) // unique by class
                                                           .values();

        colMapTypes.forEach((ThrowingConsumer<Persistable, Exception>) mapType -> {

            resultSet.beforeFirst();

            SqlBuilder mapTypeBuilder = SqlBuilder.of(mapType);
            ResultSetMapper<Persistable> mapTypeMapper = ResultSetMapper.of(mapType, mapTypeBuilder); // TOIMPROVE: change to CachedRowSet when the bugs are gone from it; allows detached processing, currently bugged due to tableNameAlias.field referring to something entirely else.
            List<Persistable> mappedPersistables = mapTypeMapper.map(resultSet);

            // narrow down by mappingType and reference values i.e. ArrayList <- ArrayList && person.id <- todo.personsId
            oneToManyBuilders.stream()
                             .filter(refColBuilder -> isMappingTypeSameAsRefType(mapType, refType, refColBuilder))
                             .forEach((ThrowingConsumer<FieldBuilder, Exception>) colBuilder -> {

                List<Persistable> filteredAndMappedPersistables = filterByReferenceValues(newTypeBuilder, mappedPersistables).collect(Collectors.toList());

                colBuilder.setFieldValue(newType, filteredAndMappedPersistables);
            });
        });
    }

    private void mapOneToOneAndAssignByMatchingReferenceValues(ResultSet resultSet, T newType, SqlBuilder newTypeBuilder) {

        List<FieldBuilder> oneToOneBuilders = refSqlBuilder.getOneToOneBuilders();
        List<Persistable> mapTypes = refSqlBuilder.getReferencePersistables().collect(Collectors.toList());

        mapTypes.forEach((ThrowingConsumer<Persistable, Exception>) mapType -> {

            resultSet.beforeFirst();

            SqlBuilder mapTypeBuilder = SqlBuilder.of(mapType);
            ResultSetMapper<Persistable> mapTypeMapper = ResultSetMapper.of(mapType, mapTypeBuilder);
            List<Persistable> mappedPersistables = mapTypeMapper.map(resultSet);

            oneToOneBuilders.stream()
                            .forEach((ThrowingConsumer<FieldBuilder, Exception>) refOneBuilder -> {

                filterByReferenceValues(newTypeBuilder, mappedPersistables).findFirst()
                                                                           .ifPresent(mappedPersistable -> {

                    refOneBuilder.setFieldValue(newType, mappedPersistable);
                });

            });
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

    // TOIMPROVE: replace with a better solution: this may come back to bite
    private List<T> removeDuplicatesByPrimaryKey(List<T> persistables) {

        List<T> results;

        if (persistables != null && persistables.size() > 0) {

            FieldBuilder pkBuilder = refSqlBuilder.getPrimaryKeyBuilder();

            Collection<T> uniquePersistables;
            uniquePersistables = persistables.stream()
                                             .collect(Collectors.toMap(t -> pkBuilder.getFieldValue(t), t -> t, (t, v) -> t))
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
