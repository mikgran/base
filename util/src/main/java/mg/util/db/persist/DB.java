package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.unwrapCauseAndRethrow;
import static mg.util.validation.rule.ValidationRule.CONNECTION_NOT_CLOSED;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.validation.Validator;

/**
 * A convenience class to create, drop a table of Type T, to save or remove a
 * row corresponding the provided Type <T extends Persistable> object.
 */
public class DB {

    private Connection connection;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructs the DB<?>.
     *
     * @param connection
     *            An open database connection. Any attempts on a closed
     *            connection will cause SQL exceptions.
     */
    public DB(Connection connection) throws IllegalArgumentException {

        PropertyConfigurator.configure("log4j.properties");

        new Validator().add("connection",
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
     * mg.util.db.db.annotation package classes.
     * 
     * @throws DBValidityException
     */
    public <T extends Persistable> void createTable(T t) throws SQLException, DBValidityException {

        SqlBuilder sqlBuilder = new SqlBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for table create: " + sqlBuilder.buildCreateTable());
            statement.executeUpdate(sqlBuilder.buildCreateTable());
        }
    }

    public <T extends Persistable> void dropTable(T t) throws SQLException, DBValidityException {

        SqlBuilder sqlBuilder = new SqlBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for table drop: " + sqlBuilder.buildDropTable());
            statement.executeUpdate(sqlBuilder.buildDropTable());
        }
    }

    public <T extends Persistable> void findBy() {
        // TODO Auto-generated method stub
    }

    // TOIMPROVE: return the removed object from the database.
    // TOIMPROVE: guard against objects without proper ids
    public <T extends Persistable> void remove(T t) throws SQLException, DBValidityException {

        SqlBuilder sqlBuilder = new SqlBuilder(t);
        String removeSql = sqlBuilder.buildDelete();

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for remove: " + removeSql);
            statement.executeUpdate(removeSql);
        }

    }

    public <T extends Persistable> void save(T t) throws SQLException, DBValidityException {

        SqlBuilder sqlBuilder = new SqlBuilder(t);

        if (t.getId() > 0) {
            doUpdate(t, sqlBuilder);
        } else {
            doInsert(t, sqlBuilder);
        }

        cascadeUpdate(t, sqlBuilder);
        // TOIMPROVE: check for dirty flag for all fields except collections
    }

    private <T extends Persistable> void cascadeUpdate(T t, SqlBuilder sqlBuilder) throws SQLException {

        if (sqlBuilder.getCollectionBuilders().size() > 0) {
            logger.debug("Cascade update for: " + t.getClass().getName());

            // in case user has tagged a Collection of non Persistable classes with i.e. @OneToMany guard against that:
            // TOIMPROVE: handle every type of collection: List, Set, Map
            // TODO handle id transfer: Person 1 -> n Todo (references Person.id)
            try {
                sqlBuilder.getCollectionBuilders()
                            .stream()
                            .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                            .filter(object -> object instanceof Persistable)
                            .map(Persistable.class::cast)
                            .forEach((ThrowingConsumer<Persistable>) persistable -> save(persistable));

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

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getValue()));
                preparedStatement.setObject(i++, fieldBuilder.getValue());
            }

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            t.setId(generatedKeys.getInt(1));
        }
    }

    private <T extends Persistable> void doUpdate(T t, SqlBuilder sqlBuilder) throws SQLException {

        String updateSql = sqlBuilder.buildUpdate();
        logger.debug("SQL for update: " + updateSql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            for (FieldBuilder fieldBuilder : sqlBuilder.getFieldBuilders()) {

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getValue()));
                preparedStatement.setObject(i++, fieldBuilder.getValue());
            }

            preparedStatement.executeUpdate();
        }
    }

}
