package mg.util.db.persist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.proxy.DBProxy;
import mg.util.db.persist.proxy.DBProxyParameters;
import mg.util.functional.consumer.ThrowingConsumer;

public class ResultSetLazyMapper<T extends Persistable> extends ResultSetMapper<T> {

    public class LazyParameters {

        public final FieldBuilder fieldBuilder;
        public final Object fieldBuilderValue;

        public LazyParameters(FieldBuilder fieldBuilder, Object fieldBuilderValue) {
            super();
            this.fieldBuilder = fieldBuilder;
            this.fieldBuilderValue = fieldBuilderValue;
        }
    }

    public ResultSetLazyMapper(T refType, SqlBuilder sqlBuilder) {
        super(refType, sqlBuilder);
    }

    public ResultSetLazyMapper(T refType, SqlBuilder sqlBuilder, DB db) {
        super(refType, sqlBuilder, db);
    }

    public T mapOne(ResultSet resultSet) throws SQLException, DBMappingException, DBValidityException {

        validateResultSet(resultSet);

        T newType = null;

        if (resultSet.next()) {

            newType = buildNewInstanceFrom(resultSet, refType);

            buildAndAssignOneToOneProxies(resultSet, newType, refType);

            buildAndAssignOneToManyProxies(resultSet, newType, refType);
        }

        return newType;
    }

    @SuppressWarnings("unchecked")
    private void buildAndAssignOneToManyProxies(ResultSet resultSet, T newType, T refType) throws DBValidityException {

        refSqlBuilder.getOneToManyBuilders()
                     .stream()
                     .map(colBuilder -> new LazyParameters(colBuilder, colBuilder.getFieldValue(refType)))
                     .filter(params -> params.fieldBuilderValue instanceof List<?> &&
                                       ((List<?>) params.fieldBuilderValue).size() > 0 &&
                                       ((List<?>) params.fieldBuilderValue).get(0) instanceof Persistable)
                     .forEach((ThrowingConsumer<LazyParameters, Exception>) params -> {

                         List<Persistable> list = new ArrayList<Persistable>((List<Persistable>) params.fieldBuilderValue);
                         Persistable refPersistable = (Persistable) list.get(0);
                         if (refPersistable != null) {

                             SqlBuilder subRefBuilder = SqlBuilderFactory.of(refPersistable);
                             String selectByRefIds = refSqlBuilder.buildSelectByRefIds(subRefBuilder);

                             DBProxyParameters<List<Persistable>> listProxyParameters;
                             listProxyParameters = new DBProxyParameters<List<Persistable>>(db,
                                                                                            new ArrayList<Persistable>(),
                                                                                            selectByRefIds,
                                                                                            refPersistable);
                             List<Persistable> listProxy = DBProxy.newList(listProxyParameters);

                             params.fieldBuilder.setFieldValue(newType, listProxy);
                         }
                     });
    }

    private void buildAndAssignOneToOneProxies(ResultSet resultSet, T newType, T refType) {

        refSqlBuilder.getOneToOneBuilders()
                     .stream()
                     .map(builder -> new LazyParameters(builder, builder.getFieldValue(refType)))
                     .filter(params -> params.fieldBuilderValue instanceof Persistable)
                     .forEach((ThrowingConsumer<LazyParameters, Exception>) params -> {

                         Persistable refPersistable = (Persistable) params.fieldBuilderValue;
                         SqlBuilder subRefBuilder = SqlBuilderFactory.of(refPersistable);
                         String selectByRefIds = refSqlBuilder.buildSelectByRefIds(subRefBuilder);

                         DBProxyParameters<Persistable> parameters;
                         parameters = new DBProxyParameters<Persistable>(db,
                                                                         refPersistable,
                                                                         selectByRefIds,
                                                                         refPersistable);

                         Persistable instanceProxy = DBProxy.newInstance(parameters);

                         params.fieldBuilder.setFieldValue(newType, instanceProxy); // may explode
                     });

    }
}
