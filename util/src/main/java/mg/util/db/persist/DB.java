package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.hasContent;
import static mg.util.Common.unwrapCauseAndRethrow;
import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.rule.ValidationRule.CONNECTION_NOT_CLOSED;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.functional.function.ThrowingBiFunction;
import mg.util.validation.Validator;

/**
 * A convenience class to create, drop a table of Type T, to save or remove a
 * row corresponding the provided Type {@code<T extends Persistable>} object.<br><br>
 *
 * The type T to is required to have at least &#64;Table and one id annotation and one
 * field annotation like &#64;VarChar in order for the DB to be able to process the type.
 *
An example of a valid processable {@code<T extends Persistable>} skeleton class:
<pre>
&#64;Table(name = "persons")
public class Person extends Persistable {

    &#64;Id
    public long id;

    &#64;VarChar
    public String firstName = "";
}

Usage example:
    Person person = new Person();
    DB db = new DB(connection);
    db.save(person);
</pre>
 */
public class DB {

    private Connection connection;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructs the DB<?>.
     *
     * @param connection An open database connection. Any attempts on a closed
     *     connection will cause SQL exceptions.
     */
    public DB(Connection connection) throws IllegalArgumentException {

        Validator.of("connection",
                     connection,
                     NOT_NULL,
                     CONNECTION_NOT_CLOSED)
                 .validate();

        this.connection = connection;
    }

    @SuppressWarnings("unused")
    private DB() {
    }

    /**
     * Creates a table from Type T using annotation @Table(name="tableName") and
     * fields annotated with @VarChar or other viable field annotations. See
     * mg.util.db.persist.annotation package classes.
     */
    public <T extends Persistable> void createTable(T t) throws SQLException, DBValidityException {

        SqlBuilder sqlBuilder = SqlBuilder.of(t);

        try (Statement statement = connection.createStatement()) {

            String createTableSql = sqlBuilder.buildCreateTable();
            logger.debug("SQL for table create: " + createTableSql);
            statement.executeUpdate(createTableSql);
        }
    }

    public void debug(Class<?> expectedType, Object object, String message) {
        validateNotNull("object can not be null.", object);
        validateNotNull("expectedType can not be null.", expectedType);

        if (hasContent(message) && object.getClass() == expectedType) {
            logger.info(message);
        }
    }

    public <T extends Persistable> void dropTable(T t) throws SQLException, DBValidityException {

        SqlBuilder sqlBuilder = SqlBuilder.of(t);

        try (Statement statement = connection.createStatement()) {

            String dropTableSql = sqlBuilder.buildDropTable();
            logger.debug("SQL for table drop: " + dropTableSql);
            statement.executeUpdate(dropTableSql);
        }
    }

    public <T extends Persistable> List<T> findAllBy(T t) throws SQLException, DBValidityException, ResultSetMapperException {

        return findBy(t, (resultSetMapper, resultSet) -> resultSetMapper.map(resultSet));
    }

    public <T extends Persistable> T findBy(T t) throws SQLException, DBValidityException, ResultSetMapperException {

        return findBy(t, (resultSetMapper, resultSet) -> resultSetMapper.mapOne(resultSet));
    }

    public <T extends Persistable> T findById(T t) throws SQLException, DBValidityException, ResultSetMapperException {

        SqlBuilder sqlBuilder = SqlBuilder.of(t);
        ResultSetMapper<T> resultSetMapper = ResultSetMapper.of(t);

        try (Statement statement = connection.createStatement()) {

            String findByIdSql = sqlBuilder.buildSelectByIds();
            logger.debug("SQL for select by id: " + findByIdSql);
            ResultSet resultSet = statement.executeQuery(findByIdSql);

            return resultSetMapper.mapOne(resultSet);
        }
    }

    // TOIMPROVE: return the removed object from the database.
    // TOIMPROVE: guard against objects without proper ids
    public <T extends Persistable> void remove(T t) throws SQLException, DBValidityException {

        SqlBuilder sqlBuilder = SqlBuilder.of(t);

        try (Statement statement = connection.createStatement()) {

            String removeSql = sqlBuilder.buildDelete();
            logger.debug("SQL for remove: " + removeSql);
            statement.executeUpdate(removeSql);
        }
    }

    /**
     * Saves type T object to the database by creating SQL insert or update statement
     * corresponding the T object fields.
     */
    public <T extends Persistable> void save(T t) throws SQLException, DBValidityException {

        saveAll(t, SqlBuilder.of(t));
    }

    protected <T extends Persistable> void refer(SqlBuilder fromSqlBuilder, SqlBuilder toSqlBuilder) throws SQLException, DBValidityException {

        List<FieldBuilder> toBuilders = toSqlBuilder.getForeignKeyBuilders();
        List<FieldBuilder> fromBuilders = fromSqlBuilder.getFieldBuilders();

        try {
            toBuilders.stream()
                      .filter(fk -> fk instanceof ForeignKeyBuilder)
                      .map(fk -> (ForeignKeyBuilder) fk)
                      .forEach(fk -> {

                          fromBuilders.stream()
                                      .filter(fb -> fromSqlBuilder.getTableName().equals(fk.getReferences()) &&
                                                    fb.getName().equals(fk.getField()))
                                      .findFirst()
                                      .ifPresent(fb -> {

                              fk.setFieldValue(fb.getValue());
                              fk.refresh();
                          });
                      });
        } catch (RuntimeException e) {
            unwrapCauseAndRethrow(e);
        }
    }

    private <T extends Persistable> void doCascadingSave(T t, SqlBuilder sqlBuilder) throws SQLException {

        if (sqlBuilder.getCollectionBuilders().size() > 0) {
            logger.debug("Cascade update for: " + t.getClass().getName());

            // in case user has tagged a Collection of non Persistable classes with i.e. @OneToMany guard against that:
            // TOIMPROVE: introduce a number of objects cap -> i.e. no endless loops or complex hierarchy revisits of same objects.
            try {
                sqlBuilder.getCollectionBuilders()
                          .stream()
                          .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                          .filter(object -> object instanceof Persistable)
                          .map(object -> (Persistable) object)
                          .forEach((ThrowingConsumer<Persistable, Exception>) persistable -> save(sqlBuilder, persistable));

            } catch (RuntimeException e) {
                // TOIMPROVE: find another way of dealing with unthrowing functional consumers
                // catch the ThrowingConsumers RuntimeException from save() -> unwrap and delegate
                unwrapCauseAndRethrow(e);
            }
        }
    }

    private <T extends Persistable> void doInsert(T t, SqlBuilder sqlBuilder) throws SQLException {

        String insertSql = sqlBuilder.buildInsert();
        logger.debug("SQL for insert: " + insertSql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            for (FieldBuilder fieldBuilder : sqlBuilder.getFieldBuilders()) {

                if (fieldBuilder.isIdField()) {
                    continue;
                }

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getValue()));
                preparedStatement.setObject(i++, fieldBuilder.getValue());
            }

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int j = 1;
                for (FieldBuilder fieldBuilder : sqlBuilder.getFieldBuilders()) {

                    if (!fieldBuilder.isIdField()) {
                        continue;
                    }
                    fieldBuilder.setFieldValue(generatedKeys.getLong(j++));
                }
            } else {
                throw new SQLException("Unable to obtain generated key for insertSql: " + insertSql);
            }
            sqlBuilder.refreshIdBuilders();
        }
    }

    private <T extends Persistable> void doUpdate(T t, SqlBuilder sqlBuilder) throws SQLException {

        String updateSql = sqlBuilder.buildUpdate();
        logger.debug("SQL for update: " + updateSql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            for (FieldBuilder fieldBuilder : sqlBuilder.getFieldBuilders()) {

                if (fieldBuilder.isIdField()) {
                    continue;
                }

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getValue()));
                preparedStatement.setObject(i++, fieldBuilder.getValue());
            }

            preparedStatement.executeUpdate();
        }
    }

    private <T extends Persistable, R> R findBy(T t, ThrowingBiFunction<ResultSetMapper<T>, ResultSet, R, Exception> function) throws DBValidityException, SQLException {

        SqlBuilder sqlBuilder = SqlBuilder.of(t);
        ResultSetMapper<T> resultSetMapper = ResultSetMapper.of(t);

        try (Statement statement = connection.createStatement()) {

            String findByFieldsSql = sqlBuilder.buildSelectByFields();
            logger.debug("SQL for select by fields: " + findByFieldsSql);
            ResultSet resultSet = statement.executeQuery(findByFieldsSql);

            // resultSetMapper.setMappingJoinQuery(findByFieldsSql.contains("JOIN")); // TOIMPROVE: sqlBuilder.buildSelectByFields() returns an wrapper having type and sql string.

            R result = null;
            try {
                result = function.apply(resultSetMapper, resultSet);

            } catch (RuntimeException e) {
                unwrapCauseAndRethrow(e);
            }
            return result;
        }
    }

    private <T extends Persistable> void save(SqlBuilder fromBuilder, T t) throws SQLException, DBValidityException {

        SqlBuilder toBuilder = SqlBuilder.of(t);

        refer(fromBuilder, toBuilder);

        saveAll(t, toBuilder);
    }

    private <T extends Persistable> void saveAll(T t, SqlBuilder toBuilder) throws SQLException {
        if (t.isFetched()) {
            doUpdate(t, toBuilder);
        } else {
            doInsert(t, toBuilder);
        }
        doCascadingSave(t, toBuilder);
    }

}
