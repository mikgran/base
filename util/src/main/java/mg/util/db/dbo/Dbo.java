package mg.util.db.dbo;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static mg.util.Common.hasContent;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.dbo.annotation.Table;
import mg.util.db.dbo.annotation.VarChar;
import mg.util.validation.Validator;

/**
 * A convenience class to create and drop a table of Type T and to persist or
 * remove a row corresponding the provided Type T object.
 *
 * @param <T>
 *            Type T object used to persist, remove, create a table or remove a
 *            table.
 */
public class Dbo<T> {

    private Logger logger = LoggerFactory.getLogger(Dbo.class);
    private Connection connection;

    @SuppressWarnings("unused")
    private Dbo() {
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
    public Dbo(Connection connection) throws DboValidityException {

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
     * @throws DboValidityException
     */
    public void createTable(T t) throws SQLException, DboValidityException {

        TableAnnotationToSqlBuilder tableSqlBuilder = new TableAnnotationToSqlBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table create: " + tableSqlBuilder.getCreateSql());
            statement.executeUpdate(tableSqlBuilder.getCreateSql());
        }
    }

    public void dropTable(T t) throws SQLException, DboValidityException {

        TableAnnotationToSqlBuilder tableSqlBuilder = new TableAnnotationToSqlBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table drop: " + tableSqlBuilder.getDropSql());
            statement.executeUpdate(tableSqlBuilder.getDropSql());
        }
    }

    public void save(T t) throws SQLException, DboValidityException {

        TableAnnotationToSqlBuilder tableSqlBuilder = new TableAnnotationToSqlBuilder(t);

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table insert: " + tableSqlBuilder.getInsertSql());
            statement.executeUpdate(tableSqlBuilder.getInsertSql());
        }
    }

    private class TableAnnotationToSqlBuilder {

        private T t;
        private String tableName;
        private String createTableSql = "";
        private String dropTableSql = "";
        private List<FieldAnnotationToSqlBuilder> fieldAnnotationToSqlBuilders = new ArrayList<FieldAnnotationToSqlBuilder>();

        public TableAnnotationToSqlBuilder(T t) throws DboValidityException {
            this.t = t;
            tableName = getTableNameFromAnnotation(t);

            if (!hasContent(tableName)) {
                throw new DboValidityException("Type T has no @Table annotation.");
            }

            fieldAnnotationToSqlBuilders = getFieldAnnotationToSqlBuilders(t);

            if (!hasContent(fieldAnnotationToSqlBuilders)) {
                throw new DboValidityException("Type T has no field annotations.");
            }

            String fieldsSql = fieldAnnotationToSqlBuilders.stream()
                                                           .map(a -> a.getFieldSql())
                                                           .collect(Collectors.joining(", "));

            // TOIMPROVE: manual id fields, setting primary key
            createTableSql = format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %s, PRIMARY KEY(id));", tableName, fieldsSql);

            dropTableSql = format("DROP TABLE IF EXISTS %s;", tableName);
        }

        private String buildInsertSql(T t, String tableName, List<FieldAnnotationToSqlBuilder> fieldAnnotationToSqlBuilders) {

            String sqlColumns = fieldAnnotationToSqlBuilders.stream()
                                                            .map(a -> a.getFieldName())
                                                            .collect(Collectors.joining(","));

            String sqlValues = fieldAnnotationToSqlBuilders.stream()
                                                           .map(a -> a.getFieldValue().toString())
                                                           .collect(Collectors.joining(","));

            return format("INSERT INTO %s (%s) VALUES(%s);", tableName, sqlColumns, sqlValues);
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

        private List<FieldAnnotationToSqlBuilder> getFieldAnnotationToSqlBuilders(T t) {

            List<FieldAnnotationToSqlBuilder> builders;
            builders = stream(t.getClass().getDeclaredFields())
                                                               .map(a -> new FieldAnnotationToSqlBuilder(t, a))
                                                               .filter(a -> a != null)
                                                               .filter(a -> a.isDboField())
                                                               .collect(Collectors.toList());

            return builders;
        }

        public String getCreateSql() {
            return createTableSql;
        }

        public String getDropSql() {
            return dropTableSql;
        }

        public String getInsertSql() {
            return buildInsertSql(t, tableName, fieldAnnotationToSqlBuilders);
        }
    }

    private class FieldAnnotationToSqlBuilder {

        private T t;
        private String sql = "";
        private String fieldName = "";
        private String fieldLength;
        private boolean notNull = true;
        private FieldType fieldType = FieldType.NON_DBO_FIELD;
        private Object fieldValue = null;

        public FieldAnnotationToSqlBuilder(T t, Field declaredField) {

            this.t = t;
            fieldName = declaredField.getName();
            Annotation[] annotations = declaredField.getAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof VarChar) {
                    VarChar varChar = (VarChar) annotation;
                    fieldType = FieldType.VARCHAR;
                    fieldLength = varChar.length();
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
