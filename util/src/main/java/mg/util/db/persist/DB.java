package mg.util.db.persist;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static mg.util.Common.hasContent;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;
import mg.util.stream.ExceptionUtil;
import mg.util.validation.Validator;

/**
 * A convenience class to create and drop a table of Type T and to persist or
 * remove a row corresponding the provided Type T object.
 *
 * @param <T>
 *            Type T object used to persist, remove, create a table or remove a
 *            table.
 */
public class DB<T extends Persistable> {

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
    public void createTable(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table create: " + tableBuilder.getCreateSql());
            statement.executeUpdate(tableBuilder.getCreateSql());
        }
    }

    public void dropTable(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table drop: " + tableBuilder.getDropSql());
            statement.executeUpdate(tableBuilder.getDropSql());
        }
    }

    public void save(T t) throws SQLException, DbValidityException {

        TableBuilder tableBuilder = new TableBuilder(t);
        String insertSql = tableBuilder.getInsertSql();

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {

            // update with an id, insert otherwise
            if (t.getId() > 0) {

                logger.info("", tableBuilder.getUpdateSql());
                // TODO

            } else {

                logger.info("SQL for table insert: " + insertSql);

                List<FieldBuilder> fieldBuilders = tableBuilder.getFieldBuilders();

                int i = 0;
                for (FieldBuilder fieldBuilder : fieldBuilders) {
                    preparedStatement.setObject(i, fieldBuilder.getFieldValue());
                }

                preparedStatement.executeUpdate(insertSql);

            }
        }
    }

    private class TableBuilder {

        private String tableName;
        private String createTableSql = "";
        private String dropTableSql = "";
        private String insertSql = "";
        private List<FieldBuilder> fieldBuilders = new ArrayList<FieldBuilder>();

        public TableBuilder(T t) throws DbValidityException {

            tableName = getTableNameAndValidate(t);

            fieldBuilders = getBuildersAndValidate(t);

            createTableSql = buildCreateTable(fieldBuilders);

            dropTableSql = format("DROP TABLE IF EXISTS %s;", tableName);

            insertSql = buildInsertSql(tableName, fieldBuilders);
        }

        private String buildInsertSql(String tableName, List<FieldBuilder> fieldBuilders) {

            String sqlColumns = fieldBuilders.stream()
                                             .map(a -> a.getFieldName())
                                             .collect(Collectors.joining(","));

            String questionMarks = fieldBuilders.stream()
                                                .map(a -> "?")
                                                .collect(Collectors.joining(","));

            return format("INSERT INTO %s (%s) VALUES(%s);", tableName, sqlColumns, questionMarks);
        }

        private String getTableNameAndValidate(T t) throws DbValidityException {
            String tableName = getTableNameFromAnnotation(t);

            if (!hasContent(tableName)) {
                throw new DbValidityException("Type T has no @Table annotation.");
            }
            return tableName;
        }

        private List<FieldBuilder> getBuildersAndValidate(T t) throws DbValidityException {

            fieldBuilders = createFieldBuilders(t);

            if (!hasContent(fieldBuilders)) {
                throw new DbValidityException("Type T has no field annotations.");
            }

            return fieldBuilders;
        }

        private String buildCreateTable(List<FieldBuilder> fieldBuilders) {

            String fieldsSql = fieldBuilders.stream()
                                            .map(a -> a.getFieldSql())
                                            .collect(Collectors.joining(", "));

            return format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %s, PRIMARY KEY(id));", tableName, fieldsSql);
        }

        private String getTableNameFromAnnotation(T t) {

            String tableName = "";
            for (Annotation classAnnotation : t.getClass().getAnnotations()) {

                if (classAnnotation instanceof Table) {

                    tableName = ((Table) classAnnotation).name();
                    break;
                }
            }
            return tableName;
        }

        private List<FieldBuilder> createFieldBuilders(T t) {

            List<FieldBuilder> builders;
            Field[] declaredFields = t.getClass().getDeclaredFields();
            builders = stream(declaredFields).map(a -> new FieldBuilder(t, a))
                                             .filter(a -> a != null)
                                             .filter(a -> a.isDboField())
                                             .collect(Collectors.toList());

            return builders;
        }

        public List<FieldBuilder> getFieldBuilders() {
            return fieldBuilders;
        }

        public String getCreateSql() {
            return createTableSql;
        }

        public String getDropSql() {
            return dropTableSql;
        }

        public String getInsertSql() {
            return insertSql;
        }

        public String getUpdateSql() {

            return "";
        }
    }

    private class FieldBuilder {

        private T t;
        private boolean notNull = true;
        private FieldType fieldType = FieldType.NON_DBO_FIELD;
        private Object fieldValue = null;
        private String fieldLength;
        private String fieldName = "";
        private String sql = "";

        public FieldBuilder(T t, Field declaredField) {

            this.t = t;
            fieldValue = "";
            fieldName = declaredField.getName();
            Annotation[] annotations = declaredField.getAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof VarChar) {
                    VarChar varChar = (VarChar) annotation;
                    fieldType = FieldType.VARCHAR;
                    fieldLength = -varChar.length();
                    notNull = varChar.notNull();
                    getFieldValue(declaredField);

                    // email VARCHAR (40) NOT NULL,
                    sql = format("%s VARCHAR(%s) %s", fieldName, fieldLength, (notNull ? "NOT NULL" : ""));
                    break;

                } else {

                    fieldType = FieldType.NON_DBO_FIELD;
                }

                // TOIMPROVE: expand field coverage: ints, dates, etc
            }
        }

        private void getFieldValue(Field declaredField) {
            try {
                declaredField.setAccessible(true);
                fieldValue = declaredField.get(t);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.error(format("Object Type %s, field named %s, declaredField.get(t) failed with %s", t.getClass(), declaredField.getName(), e.getMessage()));
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
                return "'" + fieldValue.toString() + "'";
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
