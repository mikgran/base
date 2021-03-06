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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.functional.function.ThrowingBiFunction;
import mg.util.functional.function.ThrowingFunction;
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
    private FetchPolicy fetchPolicy = FetchPolicy.EAGER;
    private Logger logger = LogManager.getLogger(this.getClass());

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
        update(t, sqlBuilder -> sqlBuilder.buildCreateTable());
    }

    public void debug(Class<?> expectedType, Object object, String message) {
        validateNotNull("object can not be null.", object);
        validateNotNull("expectedType can not be null.", expectedType);

        if (hasContent(message) && object.getClass() == expectedType) {
            logger.info(message);
        }
    }

    public <T extends Persistable> void dropTable(T t) throws SQLException, DBValidityException {
        update(t, sqlBuilder -> sqlBuilder.buildDropTable());
    }

    public <T extends Persistable> List<T> findAllBy(T t) throws SQLException, DBValidityException, DBMappingException {
        return findBy(t,
                      sqlBuilder -> sqlBuilder.buildSelectByFields(),
                      (resultSetMapper, resultSet) -> resultSetMapper.map(resultSet));
    }

    public <T extends Persistable> List<T> findAllBy(T t, String sql) throws DBValidityException, DBMappingException, SQLException {
        return findBy(t,
                      sqlBuilder -> sql,
                      (resultSetMapper, resultSet) -> resultSetMapper.map(resultSet));
    }

    public <T extends Persistable> T findBy(T t) throws SQLException, DBValidityException, DBMappingException {
        return findBy(t,
                      sqlBuilder -> sqlBuilder.buildSelectByFields(),
                      (resultSetMapper, resultSet) -> resultSetMapper.mapOne(resultSet));
    }

    public <T extends Persistable> T findBy(T t, String sql) throws DBValidityException, DBMappingException, SQLException {
        return findBy(t,
                      sqlBuilder -> sql,
                      (resultSetMapper, resultSet) -> resultSetMapper.mapOne(resultSet));
    }

    // TOIMPROVE: rename or change behavior?
    public <T extends Persistable> T findById(T t) throws SQLException, DBValidityException, DBMappingException {
        return findBy(t,
                      sqlBuilder -> sqlBuilder.buildSelectByIds(),
                      (resultSetMapper, resultSet) -> resultSetMapper.mapOne(resultSet));
    }

    public FetchPolicy getFetchPolicy() {
        return fetchPolicy;
    }

    // TOIMPROVE: return the removed object from the database.
    // TOIMPROVE: or return number of objects affected in the database (number of rows removed, not counting referencing keys)
    // TOIMPROVE: guard against objects without proper ids
    public <T extends Persistable> void remove(T t) throws SQLException, DBValidityException {
        update(t, sqlBuilder -> sqlBuilder.buildDelete());
    }

    /**
     * Saves type T object to the database by creating SQL insert or update statement
     * corresponding the T object fields.
     */
    public <T extends Persistable> void save(T t) throws SQLException, DBValidityException {
        saveAll(t, SqlBuilderFactory.of(t));
    }

    public void setFetchPolicy(FetchPolicy fetchPolicy) {
        this.fetchPolicy = fetchPolicy;
    }

    // TOIMPROVE: allows now only singular reference from table to another, consider allowing multiple references from table a to table b.
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

                              fk.setFieldValue(toSqlBuilder.getType(), fb.getFieldValue(fromSqlBuilder.getType()));
                          });
                      });
        } catch (RuntimeException e) {
            unwrapCauseAndRethrow(e);
        }
    }

    private <T extends Persistable> void doCascadingSave(T t, SqlBuilder sqlBuilder) throws SQLException {

        if (sqlBuilder.getOneToManyBuilders().size() > 0 ||
            sqlBuilder.getOneToOneBuilders().size() > 0) {
            logger.debug("Cascade update for: " + t.getClass().getName());

            // in case user has tagged a Collection of non Persistable classes with i.e. @OneToMany guard against that:
            // TOIMPROVE: introduce a number of objects cap -> i.e. no endless loops or complex hierarchy revisits of same objects.
            try {
                sqlBuilder.getOneToManyBuilders()
                          .stream()
                          .map(oneToManyBuilder -> oneToManyBuilder.getFieldValue(t))
                          .filter(object -> object instanceof Collection<?>)
                          .flatMap(object -> flattenToStream((Collection<?>) object))
                          .filter(object -> object instanceof Persistable)
                          .map(object -> (Persistable) object)
                          .forEach((ThrowingConsumer<Persistable, Exception>) persistable -> referAndSave(sqlBuilder, persistable));

                sqlBuilder.getOneToOneBuilders()
                          .stream()
                          .map(oneToOneBuilder -> oneToOneBuilder.getFieldValue(t))
                          .filter(object -> object instanceof Persistable)
                          .map(object -> (Persistable) object)
                          .forEach((ThrowingConsumer<Persistable, Exception>) persistable -> referAndSave(sqlBuilder, persistable));

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

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getFieldValue(t)));
                preparedStatement.setObject(i++, fieldBuilder.getFieldValue(t));
            }

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int j = 1;
                for (FieldBuilder fieldBuilder : sqlBuilder.getFieldBuilders()) {

                    if (!fieldBuilder.isIdField()) {
                        continue;
                    }
                    fieldBuilder.setFieldValue(t, generatedKeys.getLong(j++));
                }
            } else {
                throw new SQLException("Unable to obtain generated key for insertSql: " + insertSql);
            }
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

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getFieldValue(t)));
                preparedStatement.setObject(i++, fieldBuilder.getFieldValue(t));
            }

            preparedStatement.executeUpdate();
        }
    }

    private <T extends Persistable, R> R findBy(T t,
        ThrowingFunction<SqlBuilder, String, Exception> sqlFunction,
        ThrowingBiFunction<ResultSetMapper<T>, ResultSet, R, Exception> mapperFunction) throws DBValidityException, SQLException {

        SqlBuilder sqlBuilder = SqlBuilderFactory.of(t, this);
        ResultSetMapper<T> resultSetMapper = ResultSetMapperFactory.of(t, sqlBuilder, this);

        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            String findBySql = sqlFunction.apply(sqlBuilder);
            logger.debug("SQL for select by: " + findBySql);
            ResultSet resultSet = statement.executeQuery(findBySql);

            R result = null;
            try {
                result = mapperFunction.apply(resultSetMapper, resultSet);

            } catch (RuntimeException e) {
                unwrapCauseAndRethrow(e);
            }
            return result;
        }
    }

    private <T extends Persistable> void referAndSave(SqlBuilder fromBuilder, T t) throws SQLException, DBValidityException {

        SqlBuilder toBuilder = SqlBuilderFactory.of(t);

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

    private <T extends Persistable> void update(T t, ThrowingFunction<SqlBuilder, String, Exception> updateFunction) throws DBValidityException, SQLException {

        SqlBuilder sqlBuilder = SqlBuilderFactory.of(t);

        try (Statement statement = connection.createStatement()) {

            String updateSql = updateFunction.apply(sqlBuilder);
            logger.debug("SQL for update: " + updateSql);
            statement.executeUpdate(updateSql);
        }
    }
}
