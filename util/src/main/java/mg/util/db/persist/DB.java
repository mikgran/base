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
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;
import mg.util.validation.Validator;

/**
 * A convenience class to create, drop a table of Type T and to save or remove a
 * row corresponding the provided Type T object.
 *
 * @param <T>
 *            Type T object used to save, remove, create a table or remove a
 *            table.
 */
public class DB {

    private Logger logger = LoggerFactory.getLogger(DB.class);
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

            logger.info("SQL for table create: " + tableBuilder.getCreateSql());
            statement.executeUpdate(tableBuilder.getCreateSql());
        }
    }

    public <T extends Persistable> void dropTable(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table drop: " + tableBuilder.getDropSql());
            statement.executeUpdate(tableBuilder.getDropSql());
        }
    }

    public <T extends Persistable> void save(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

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

            logger.info("SQL for remove: " + removeSql);
            statement.executeUpdate(removeSql);
        }

    }

    private <T extends Persistable> void doInsert(T t, TableBuilder tableBuilder) throws SQLException {

        String insertSql = tableBuilder.getInsertSql();
        logger.info("SQL for insert: " + insertSql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            for (FieldBuilder fieldBuilder : tableBuilder.getFieldBuilders()) {

                logger.info(format("fieldBuilder value:: %d %s", i, fieldBuilder.getFieldValue()));
                preparedStatement.setObject(i++, fieldBuilder.getFieldValue());
            }

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            t.setId(generatedKeys.getInt(1));
        }
    }

    private <T extends Persistable> void doUpdate(T t, TableBuilder tableBuilder) throws SQLException {

        String updateSql = tableBuilder.getUpdateSql();
        logger.info("SQL for update: " + updateSql);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            for (FieldBuilder fieldBuilder : tableBuilder.getFieldBuilders()) {

                logger.info(format("fieldBuilder value:: %d %s", i, fieldBuilder.getFieldValue()));
                preparedStatement.setObject(i++, fieldBuilder.getFieldValue());
            }

            preparedStatement.executeUpdate();
        }
    }

    private class TableBuilder {

        private int id = 0;
        private String tableName;
        private List<FieldBuilder> fieldBuilders = new ArrayList<FieldBuilder>();

        public <T extends Persistable> TableBuilder(T t) throws DbValidityException {

            tableName = getTableNameAndValidate(t);
            fieldBuilders = getFieldBuildersAndValidate(t);
            id = t.getId();
        }

        private <T extends Persistable> String getTableNameAndValidate(T t) throws DbValidityException {

            Optional<Table> tableAnnotationCanditate;
            tableAnnotationCanditate = Arrays.stream(t.getClass().getAnnotations())
                                             .filter(a -> a instanceof Table)
                                             .map(Table.class::cast)
                                             .findFirst();

            if (!tableAnnotationCanditate.isPresent() ||
                !hasContent(tableAnnotationCanditate.get().name())) {

                throw new DbValidityException("Type T has no @Table annotation.");
            }

            return tableAnnotationCanditate.get().name();
        }

        private <T extends Persistable> List<FieldBuilder> getFieldBuildersAndValidate(T t) throws DbValidityException {

            List<FieldBuilder> fieldBuilders;
            fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                                  .map(a -> new FieldBuilder(t, a))
                                  .filter(a -> a != null)
                                  .filter(a -> a.isDboField())
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
                                            .map(a -> a.getFieldSql())
                                            .collect(Collectors.joining(", "));

            return format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %s, PRIMARY KEY(id));", tableName, fieldsSql);
        }

        public String getDropSql() {
            return format("DROP TABLE IF EXISTS %s;", tableName);
        }

        // TOIMPROVE: partial updates
        public String getInsertSql() {
            String sqlColumns = fieldBuilders.stream()
                                             .map(a -> a.getFieldName())
                                             .collect(Collectors.joining(", "));

            String questionMarks = fieldBuilders.stream()
                                                .map(a -> "?")
                                                .collect(Collectors.joining(", "));

            return format("INSERT INTO %s (%s) VALUES(%s);", tableName, sqlColumns, questionMarks);
        }

        public String getUpdateSql() {
            String fieldsSql = fieldBuilders.stream()
                                            .map(a -> a.getFieldName() + " = ?")
                                            .collect(Collectors.joining(", "));

            return format("UPDATE %s SET %s;", tableName, fieldsSql);
        }

        public String getRemoveSql() {
            return format("DELETE FROM %s WHERE id = %s;", tableName, id);
        }

    }

    private class FieldBuilder {

        private boolean notNull = true;
        private FieldType fieldType = FieldType.NON_DBO_FIELD;
        private Object fieldValue = null;
        private String fieldLength;
        private String fieldName = "";
        private String sql = "";

        public FieldBuilder(Object parentObject, Field declaredField) {

            fieldValue = "";
            fieldName = declaredField.getName();
            Annotation[] annotations = declaredField.getAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof VarChar) {
                    VarChar varChar = (VarChar) annotation;
                    fieldType = FieldType.VARCHAR;
                    fieldLength = varChar.length();
                    notNull = varChar.notNull();
                    getFieldValue(parentObject, declaredField);

                    // email VARCHAR (40) NOT NULL,
                    sql = format("%s VARCHAR(%s) %s", fieldName, fieldLength, (notNull ? "NOT NULL" : ""));
                    break;

                } else {

                    fieldType = FieldType.NON_DBO_FIELD;
                }

                // TOIMPROVE: expand field coverage: ints, dates, etc
            }
        }

        private void getFieldValue(Object parentObject, Field declaredField) {
            try {
                declaredField.setAccessible(true);
                fieldValue = declaredField.get(parentObject);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.error(format("Object Type %s, field named %s, declaredField.get(t) failed with:\n%s", parentObject.getClass(), declaredField.getName(), e.getMessage()));
            }
        }

        public boolean isDboField() {
            return !FieldType.NON_DBO_FIELD.equals(fieldType);
        }

        public String getFieldSql() {
            return sql;
        }

        public Object getFieldValue() {
            if (FieldType.VARCHAR.equals(fieldType)) {
                return fieldValue.toString();
            }
            return fieldValue;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    private enum FieldType {
        ID("ID"), INT("INT"), VARCHAR("VARCHAR"), NON_DBO_FIELD("NON_DBO_FIELD");
        private final String str;
        FieldType(String str) {
            this.str = str;
        }
        public String toString() {
            return str;
        }
    }

}
