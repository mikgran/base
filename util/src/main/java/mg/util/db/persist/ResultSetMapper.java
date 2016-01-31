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
        // TODO: map: lazy loading and eager loading
        while (resultSet.next()) {

            T newType = buildNewInstanceFrom(resultSet, refType);

            int row = resultSet.getRow();

            buildAndAssignRefsCascading(resultSet, newType, refType);

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

    // TOIMPROVE: add OneToOne refs handling
    private void buildAndAssignRefsCascading(ResultSet resultSet, T newType, T refType) throws DBValidityException {

        List<FieldBuilder> oneToManyBuilders = refSqlBuilder.getOneToManyBuilders();
        SqlBuilder newTypeBuilder = SqlBuilder.of(newType);

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

        List<FieldBuilder> oneToOneBuilders = refSqlBuilder.getOneToOneBuilders();
        Stream<Persistable> mapTypes = refSqlBuilder.getReferencePersistables();

        mapTypes.forEach((ThrowingConsumer<Persistable, Exception>) mapType -> {

            resultSet.beforeFirst();

            SqlBuilder mapTypeBuilder = SqlBuilder.of(mapType);
            ResultSetMapper<Persistable> mapTypeMapper = ResultSetMapper.of(mapType, mapTypeBuilder);
            Persistable mappedPersistable = mapTypeMapper.mapOne(resultSet);

            oneToOneBuilders.stream()
                            //.filter(refOneBuilder -> isMappingTypeSameAsRefType(mapType, refOneBuilder, refType))
                            .forEach((ThrowingConsumer<FieldBuilder, Exception>) refOneBuilder -> {

                if (referenceValuesMatch(newTypeBuilder, mappedPersistable)) {
                    refOneBuilder.setFieldValue(newType, mappedPersistable);
                }
            });

        });
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

    @SuppressWarnings("unchecked")
    private T newInstance(T type) throws ResultSetMapperException {

        try {
            return (T) type.getClass().newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResultSetMapperException("Exception in instantiating type T: " + e.getMessage());
        }
    }

    private boolean referenceValuesMatch(SqlBuilder referredBuilder, Persistable ref) throws DBValidityException {

        SqlBuilder referringBuilder = SqlBuilder.of(ref);
        List<FieldReference> fieldRefs = referredBuilder.getReferences(referredBuilder, referringBuilder)
                                                        .collect(Collectors.toList());
        return fieldRefs.stream()
                        .allMatch(fieldReference -> fieldReference.fieldValuesMatch());
    }

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
