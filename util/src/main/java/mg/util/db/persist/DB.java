package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.hasContent;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
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
     * Constructs the DBO<?>.
     *
     * @param connection
     *            An open database connection. Any attempts on a closed
     *            connection will cause SQL exceptions.
     * @param t
     *            Type T object that has been annotated with @Table.
     */
    public DB(Connection connection) throws DbValidityException {

        PropertyConfigurator.configure("log4j.properties");

        new Validator().add("connection", connection, NOT_NULL)
                       .validate();

        this.connection = connection;
    }

    /**
     * Creates a table from Type T using annotation @Table(name="tableName") and
     * fields annotated with @VarChar or other viable field annotations. See
     * mg.util.db.dbo.annotation package classes.
     * 
     * @throws DbValidityException
     */
    public <T extends Persistable> void createTable(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for table create: " + tableBuilder.getCreateSql());
            statement.executeUpdate(tableBuilder.getCreateSql());
        }
    }

    public <T extends Persistable> void dropTable(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.debug("SQL for table drop: " + tableBuilder.getDropSql());
            statement.executeUpdate(tableBuilder.getDropSql());
        }
    }

    public <T extends Persistable> void save(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        if (tableBuilder.isAnyFieldCollection()) {

            cascadeUpdateCollections(t, tableBuilder);
        }

        singleUpdate(t, tableBuilder);
    }

    private <T extends Persistable> void cascadeUpdateCollections(T t, TableBuilder tb) {

        logger.debug("Cascade update for: " + t.getClass().getName());
        // obtain list of T extends Persistables of all the structures
        // start save(T t) on each.

        List<Field> collections = Arrays.stream(t.getClass().getDeclaredFields())
                                        .filter(a -> tb.isFieldAcceptableCollection(a))
                                        .collect(Collectors.toList());

        // or alternative:

        // recursively call list.forEach(a -> save(a))

    }

    private <T extends Persistable> void singleUpdate(T t, TableBuilder tableBuilder) throws SQLException {
        if (t.getId() > 0) {

            doUpdate(t, tableBuilder);

        } else {

            doInsert(t, tableBuilder);
        }
    }

    // TOIMPROVE: return the removed object from the database.
    public <T extends Persistable> void remove(T t) throws SQLException, DbValidityException {

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
        private boolean isAnyFieldCollection = false;
        private String tableName;
        private List<FieldBuilder> fieldBuilders = new ArrayList<FieldBuilder>();
        private List<Class<? extends Annotation>> acceptableCollectionAnnotations = Arrays.asList(OneToMany.class);

        public <T extends Persistable> TableBuilder(T t) throws DbValidityException {

            tableName = getTableNameAndValidate(t);
            fieldBuilders = getFieldBuildersAndValidate(t);
            isAnyFieldCollection = isAnyFieldCollection(t);
            id = t.getId();
        }

        private boolean isFieldAcceptableCollection(Field field) {

            return acceptableCollectionAnnotations.stream()
                                                  .anyMatch(a -> field.isAnnotationPresent(a));
        }

        private <T extends Persistable> boolean isAnyFieldCollection(T t) {

            return Arrays.stream(t.getClass().getDeclaredFields())
                         .anyMatch(a -> isFieldAcceptableCollection(a));
        }

        private <T extends Persistable> String getTableNameAndValidate(T t) throws DbValidityException {

            Table tableAnnotation = t.getClass().getAnnotation(Table.class);
            if (tableAnnotation == null) {
                throw new DbValidityException("Type T has no @Table annotation.");
            }

            return tableAnnotation.name();
        }

        private <T extends Persistable> List<FieldBuilder> getFieldBuildersAndValidate(T t) throws DbValidityException {

            List<FieldBuilder> fieldBuilders;
            fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                                  .map(a -> FieldBuilderFactory.of(t, a))
                                  .filter(a -> a != null)
                                  .filter(a -> a.isDbField())
                                  .collect(Collectors.toList());

            if (!hasContent(fieldBuilders)) {
                throw new DbValidityException("Type T has no field annotations.");
            }

            return fieldBuilders;
        }

        public List<FieldBuilder> getFieldBuilders() {
            return fieldBuilders;
        }

        public String getCreateSql() {
            String fieldsSql = fieldBuilders.stream()
                                            .map(a -> a.getSql())
                                            .collect(Collectors.joining(", "));

            return format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %s, PRIMARY KEY(id));", tableName, fieldsSql);
        }

        public String getDropSql() {
            return format("DROP TABLE IF EXISTS %s;", tableName);
        }

        public boolean isAnyFieldCollection() {
            return isAnyFieldCollection;
        }

        // TOIMPROVE: partial updates
        public String getInsertSql() {
            String sqlColumns = fieldBuilders.stream()
                                             .map(a -> a.getName())
                                             .collect(Collectors.joining(", "));

            String questionMarks = fieldBuilders.stream()
                                                .map(a -> "?")
                                                .collect(Collectors.joining(", "));

            return format("INSERT INTO %s (%s) VALUES(%s);", tableName, sqlColumns, questionMarks);
        }

        public String getUpdateSql() {
            String fieldsSql = fieldBuilders.stream()
                                            .map(a -> a.getName() + " = ?")
                                            .collect(Collectors.joining(", "));

            return format("UPDATE %s SET %s;", tableName, fieldsSql);
        }

        public String getRemoveSql() {
            return format("DELETE FROM %s WHERE id = %s;", tableName, id);
        }

    }

}
