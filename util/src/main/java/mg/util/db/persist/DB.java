package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.hasContent;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
import mg.util.functional.consumer.ThrowingConsumer;
import mg.util.validation.Validator;

/**
 * A convenience class to create, drop a table of Type T, to save or remove a
 * row corresponding the provided Type <T extends Persistable> object.
 */
public class DB {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection connection;

    @SuppressWarnings("unused")
    private DB() {
    }

    /**
     * Constructs the DB<?>.
     *
     * @param connection
     *            An open database connection. Any attempts on a closed
     *            connection will cause SQL exceptions.
     * @param t
     *            Type T object that has been annotated with @Table.
     */
    public DB(Connection connection) throws DBValidityException {

        PropertyConfigurator.configure("log4j.properties");

        new Validator().add("connection", connection, NOT_NULL)
                       .validate();

        this.connection = connection;
    }

    /**
     * Creates a table from Type T using annotation @Table(name="tableName") and
     * fields annotated with @VarChar or other viable field annotations. See
     * mg.util.db.db.annotation package classes.
     * 
     * @throws DBValidityException
     */
    public <T extends Persistable> void createTable(T t) throws SQLException, DBValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for table create: " + tableBuilder.getCreateSql());
            statement.executeUpdate(tableBuilder.getCreateSql());
        }
    }

    public <T extends Persistable> void dropTable(T t) throws SQLException, DBValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for table drop: " + tableBuilder.getDropSql());
            statement.executeUpdate(tableBuilder.getDropSql());
        }
    }

    public <T extends Persistable> void save(T t) throws SQLException, DBValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        if (t.getId() > 0) {
            doUpdate(t, tableBuilder);
        } else {
            doInsert(t, tableBuilder);
        }

        cascadeUpdate(t, tableBuilder);
        // TOIMPROVE: check for dirty flag for all fields except collections
    }

    private <T extends Persistable> void cascadeUpdate(T t, TableBuilder tableBuilder) throws SQLException {
        if (tableBuilder.getCollectionBuilders().size() > 0) {
            logger.debug("Cascade update for: " + t.getClass().getName());

            // in case user has tagged a Collection of non Persistable classes with i.e. @OneToMany guard against that:
            // TOIMPROVE: handle every type of collection: List, Set, Map
            try {
                tableBuilder.getCollectionBuilders()
                            .stream()
                            .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                            .filter(object -> object instanceof Persistable)
                            .map(Persistable.class::cast)
                            .forEach((ThrowingConsumer<Persistable>) persistable -> save(persistable));

            } catch (RuntimeException e) {
                // TOIMPROVE: find another way of dealing with unthrowing functional consumers
                // TOIMPROVE: move to Common and add common type exceptions
                // catch the ThrowingConsumers RuntimeException from save() -> unwrap and delegate
                Throwable cause = e.getCause();
                if (cause instanceof SQLException) {
                    throw new SQLException(cause);
                }

                throw new RuntimeException(cause);
            }
        }
    }

    // TOIMPROVE: return the removed object from the database.
    // TOIMPROVE: guard against objects without proper ids
    public <T extends Persistable> void remove(T t) throws SQLException, DBValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);
        String removeSql = tableBuilder.getRemoveSql();

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for remove: " + removeSql);
            statement.executeUpdate(removeSql);
        }

    }

    private <T extends Persistable> void doInsert(T t, TableBuilder tableBuilder) throws SQLException {

        String insertSql = tableBuilder.getInsertSql();
        logger.debug("SQL for insert: " + insertSql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            for (FieldBuilder fieldBuilder : tableBuilder.getFieldBuilders()) {

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getValue()));
                preparedStatement.setObject(i++, fieldBuilder.getValue());
            }

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            t.setId(generatedKeys.getInt(1));
        }
    }

    private <T extends Persistable> void doUpdate(T t, TableBuilder tableBuilder) throws SQLException {

        String updateSql = tableBuilder.getUpdateSql();
        logger.debug("SQL for update: " + updateSql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            for (FieldBuilder fieldBuilder : tableBuilder.getFieldBuilders()) {

                logger.debug(format("fieldBuilder value:: %d %s", i, fieldBuilder.getValue()));
                preparedStatement.setObject(i++, fieldBuilder.getValue());
            }

            preparedStatement.executeUpdate();
        }
    }

    private class TableBuilder {

        private int id = 0;
        private String tableName;
        private List<FieldBuilder> fieldBuilders;
        private List<FieldBuilder> collectionBuilders;

        public <T extends Persistable> TableBuilder(T t) throws DBValidityException {

            tableName = getTableNameAndValidate(t);
            fieldBuilders = getFieldBuildersAndValidate(t);
            collectionBuilders = getCollectionBuilders(t);
            id = t.getId();
        }

        private <T extends Persistable> String getTableNameAndValidate(T t) throws DBValidityException {

            Table tableAnnotation = t.getClass().getAnnotation(Table.class);
            if (tableAnnotation == null) {
                throw new DBValidityException("Type T has no @Table annotation.");
            }

            return tableAnnotation.name();
        }

        private <T extends Persistable> List<FieldBuilder> getCollectionBuilders(T t) {
            return Arrays.stream(t.getClass().getDeclaredFields())
                         .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                         .filter(fieldBuilder -> fieldBuilder != null)
                         .filter(fieldBuilder -> fieldBuilder.isCollectionField())
                         .collect(Collectors.toList());
        }

        private <T extends Persistable> List<FieldBuilder> getFieldBuildersAndValidate(T t) throws DBValidityException {

            List<FieldBuilder> fieldBuilders;
            fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                                  .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                                  .filter(fieldBuilder -> fieldBuilder != null)
                                  .filter(fieldBuilder -> fieldBuilder.isDbField())
                                  .collect(Collectors.toList());

            if (!hasContent(fieldBuilders)) {
                throw new DBValidityException("Type T has no field annotations.");
            }

            return fieldBuilders;
        }

        public List<FieldBuilder> getFieldBuilders() {
            return fieldBuilders;
        }

        public List<FieldBuilder> getCollectionBuilders() {
            return collectionBuilders;
        }

        public String getCreateSql() {
            String fieldsSql = fieldBuilders.stream()
                                            .map(fieldBuilder -> fieldBuilder.getSql())
                                            .collect(Collectors.joining(", "));

            return format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %s, PRIMARY KEY(id));", tableName, fieldsSql);
        }

        public String getDropSql() {
            return format("DROP TABLE IF EXISTS %s;", tableName);
        }

        // TOIMPROVE: partial updates
        public String getInsertSql() {
            String sqlColumns = fieldBuilders.stream()
                                             .map(fieldBuilder -> fieldBuilder.getName())
                                             .collect(Collectors.joining(", "));

            String questionMarks = fieldBuilders.stream()
                                                .map(fieldBuilder -> "?")
                                                .collect(Collectors.joining(", "));

            return format("INSERT INTO %s (%s) VALUES(%s);", tableName, sqlColumns, questionMarks);
        }

        public String getUpdateSql() {
            String fieldsSql = fieldBuilders.stream()
                                            .map(fieldBuilder -> fieldBuilder.getName() + " = ?")
                                            .collect(Collectors.joining(", "));

            return format("UPDATE %s SET %s;", tableName, fieldsSql);
        }

        public String getRemoveSql() {
            return format("DELETE FROM %s WHERE id = %s;", tableName, id);
        }

    }

}
