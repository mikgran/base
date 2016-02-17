package mg.util.db.persist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mg.util.db.persist.field.FieldBuilder;

public class ResultSetLazyMapper<T extends Persistable> extends ResultSetMapper<T> {

    public class LazyParameters {

        FieldBuilder fieldBuilder;
        Object fieldBuilderValue;

        public LazyParameters(FieldBuilder fieldBuilder, Object fieldBuilderValue) {
            super();
            this.fieldBuilder = fieldBuilder;
            this.fieldBuilderValue = fieldBuilderValue;
        }
    }

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

    // @SuppressWarnings("unused")
    private void buildAndAssignProxies(ResultSet resultSet, T newType, T refType) throws DBValidityException {

        // SqlBuilder newTypeBuilder = SqlBuilderFactory.of(newType);

        // 1. assign proxy lists with parameters (including fetch by referring ids)
        // 2. assign OneToMany proxies for each non null collection that has at least one element
        // 3. assign OneToOne proxies for each non null oneToOne

        refSqlBuilder.getOneToManyBuilders()
                     .stream()
                     .map(colBuilder -> {
                         return new LazyParameters(colBuilder, colBuilder.getFieldValue(refType));
                     });
//                     .map(colBuilder -> colBuilder.getFieldValue(refType))
//                     .filter(object -> object instanceof List<?> && ((List<?>) object).size() > 0)
//                     .map(object -> (List<?>) object)
//                     .forEach(list -> {
//
//                         Persistable refPersistable = (Persistable) list.get(0);
//                         if (refPersistable != null) {
//
//                             //
//                         }
//
//                     });

    }
}
