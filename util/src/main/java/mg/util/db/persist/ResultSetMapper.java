
package mg.util.db.persist;

import static mg.util.Common.hasContent;
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

    protected DB db;
    protected SqlBuilder refSqlBuilder;
    protected T refType;

    public ResultSetMapper(T refType, SqlBuilder sqlBuilder) {
        this.refSqlBuilder = validateNotNull("sqlBuilder", sqlBuilder);
        this.refType = validateNotNull("refType", refType);
    }

    public ResultSetMapper(T refType, SqlBuilder sqlBuilder, DB db) {
        this.refSqlBuilder = validateNotNull("sqlBuilder", sqlBuilder);
        this.refType = validateNotNull("refType", refType);
        this.db = validateNotNull("db", db);
    }

    public List<T> map(ResultSet resultSet) throws DBValidityException, DBMappingException, SQLException {

        validateResultSet(resultSet);

        List<T> results = new ArrayList<>();

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
     * no rows are present in the resultSet, a null is returned.
     */
    public T mapOne(ResultSet resultSet) throws SQLException, DBMappingException, DBValidityException {

        validateResultSet(resultSet);

        T newType = null;

        if (resultSet.next()) {

            newType = buildNewInstanceFrom(resultSet, refType);

            buildAndAssignRefsCascading(resultSet, newType, refType);
        }

        return newType;
    }

    public List<T> partialMap(ResultSet resultSet) throws DBValidityException, DBMappingException, SQLException {
        throw new NotYetImplementedException("ResultSetMapper.partialMap has not been implemented yet.");
    }

    public T partialMapOne(ResultSet resultSet) {
        throw new NotYetImplementedException("ResultSetMapper.partialMap has not been implemented yet.");
    }

    protected void buildAndAssignRefsCascading(ResultSet resultSet, T newType, T refType) throws DBValidityException {

        SqlBuilder newTypeBuilder = SqlBuilderFactory.of(newType);

        mapOneToManyAndAssignByMatchingReferenceValues(resultSet, newType, refType, newTypeBuilder);

        mapOneToOneAndAssignByMatchingReferenceValues(resultSet, newType, refType, newTypeBuilder);
    }

    protected T buildNewInstanceFrom(ResultSet resultSet, T type) throws DBMappingException, SQLException {

        T newType = newInstance(type);

        // TOIMPROVE: move tableNameAlias building to caller.
        AliasBuilder aliasBuilder = refSqlBuilder.getAliasBuilder();
        String tableName = refSqlBuilder.getTableName();
        String tableNameAlias = aliasBuilder.aliasOf(tableName);
        List<FieldBuilder> fieldBuilders = refSqlBuilder.getFieldBuilders();

        try {
            fieldBuilders.forEach((ThrowingConsumer<FieldBuilder, Exception>) fieldBuilder -> {

                String fieldNameString = tableNameAlias + "." + fieldBuilder.getName();
                Object object = resultSet.getObject(fieldNameString);
                // System.out.println("buildNewInstanceFrom::object: " + object + ", fieldBuilder.fieldName: " + fieldBuilder.getDeclaredField().getName());
                fieldBuilder.setFieldValue(newType, object);
            });

            newType.setFetched(true);

        } catch (RuntimeException e) {
            unwrapCauseAndRethrow(e);
        }

        return newType;
    }

    protected void validateResultSet(ResultSet resultSet) throws SQLException, DBMappingException {
        validateNotNull("resultSet", resultSet);

        if (resultSet.isClosed()) {
            throw new DBMappingException("ResultSet can not be closed.");
        }
    }

    private Stream<Persistable> filterByReferenceValues(SqlBuilder typeBuilder, List<Persistable> mappedForRef) throws DBValidityException {
        return mappedForRef.stream()
                           .filter((ThrowingPredicate<Persistable, Exception>) mappedPersistable -> {

                               return refSqlBuilder.getReferences(typeBuilder, SqlBuilderFactory.of(mappedPersistable))
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

    private void mapOneToManyAndAssignByMatchingReferenceValues(ResultSet resultSet, T newType, T refType, SqlBuilder newTypeBuilder) throws DBValidityException {

        List<FieldBuilder> oneToManyBuilders = refSqlBuilder.getOneToManyBuilders();
        Collection<Persistable> colMapTypes = refSqlBuilder.getReferenceCollectionPersistables(refType)
                                                           .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p)) // unique by class
                                                           .values();

        colMapTypes.forEach((ThrowingConsumer<Persistable, Exception>) mapType -> {

            resultSet.beforeFirst();

            SqlBuilder mapTypeBuilder = SqlBuilderFactory.of(mapType);
            ResultSetMapper<Persistable> mapTypeMapper = ResultSetMapperFactory.of(mapType, mapTypeBuilder); // TOIMPROVE: change to CachedRowSet when the bugs are gone from it; allows detached processing, currently bugged due to tableNameAlias.field referring to something entirely else.
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

    private void mapOneToOneAndAssignByMatchingReferenceValues(ResultSet resultSet, T newType, T refType, SqlBuilder newTypeBuilder) throws DBValidityException {

        List<FieldBuilder> oneToOneBuilders = refSqlBuilder.getOneToOneBuilders();
        List<Persistable> mapTypes = refSqlBuilder.getReferencePersistables(refType).collect(Collectors.toList());

        mapTypes.forEach((ThrowingConsumer<Persistable, Exception>) mapType -> {

            resultSet.beforeFirst();

            SqlBuilder mapTypeBuilder = SqlBuilderFactory.of(mapType);
            ResultSetMapper<Persistable> mapTypeMapper = ResultSetMapperFactory.of(mapType, mapTypeBuilder);
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
    private T newInstance(T type) throws DBMappingException {

        try {
            return (T) type.getClass().newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new DBMappingException("Exception in instantiating type T: " + e.getMessage());
        }
    }

    // TOIMPROVE: replace with a better solution: this may come back to bite
    private List<T> removeDuplicatesByPrimaryKey(List<T> persistables) {

        List<T> results;

        if (hasContent(persistables)) {

            FieldBuilder pkBuilder = refSqlBuilder.getPrimaryKeyBuilder();

            Collection<T> uniquePersistables;
            uniquePersistables = persistables.stream()
                                             .collect(Collectors.toMap(t -> pkBuilder.getFieldValue(t), t -> t, (t, v) -> t))
                                             .values();

            persistables.retainAll(uniquePersistables); // keep the original order with this

            results = persistables;

        } else {

            results = persistables;
        }

        return results;
    }

}
