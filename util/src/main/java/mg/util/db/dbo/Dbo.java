package mg.util.db.dbo;

import static java.lang.String.format;
import static mg.util.Common.hasContent;
import static mg.util.validation.rule.ValidationRule.NOT_NULL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.dbo.annotation.Table;
import mg.util.db.dbo.annotation.VarChar;
import mg.util.validation.Validator;

/**
 * A convenience class to create and drop a table of Type T and to persist or
 * remove a row corresponding this object.
 *
 * @param <T>
 *            Type T object used to persist, remove, create a table or remove a
 *            table.
 */
public class Dbo<T> {

    private T t;
    private Logger logger = LoggerFactory.getLogger(Dbo.class);
    private Connection connection;

    private TableAnnotationToSqlBuilder tableSqlBuilder;

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
     * @throws DboValidityException
     *             if @Table and atleast one field annotation was missing from
     *             Type T or there was an sql exception.
     * @throws InvalidArgumentException
     *             in the case that connection or t was a null.
     */
    public Dbo(Connection connection, T t) throws DboValidityException {

        PropertyConfigurator.configure("log4j.properties");
        
        new Validator().add("connection", connection, NOT_NULL)
                       .add("t", t, NOT_NULL)
                       .validate();

        this.t = t;
        this.connection = connection;

        tableSqlBuilder = new TableAnnotationToSqlBuilder(this.t);
    }

    /**
     * Creates a table from Type T using annotation @Table(name="tableName") and
     * fields annotated with @VarChar or other viable field annotations. See
     * mg.util.db.dbo.annotation package classes.
     *
     * @throws SQLException
     *             if any database error occured. For instance: user tried to
     *             apply @VarChar to an int field.
     */
    public void createTable() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table create: " + tableSqlBuilder.getCreateSql());
            statement.executeUpdate(tableSqlBuilder.getCreateSql());
        }
    }

    public void dropTable() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            logger.info("SQL for table drop: " + tableSqlBuilder.getDropSql());
            statement.executeUpdate(tableSqlBuilder.getDropSql());
        }
    }

    public void persist() {

        // throw exception
    }

    private class TableAnnotationToSqlBuilder {

        private String tableName;
        private String createTableSql = "";
        private String dropTableSql = "";
        private List<FieldAnnotationToSqlBuilder> fieldAnnotationToSqlBuilders = new ArrayList<FieldAnnotationToSqlBuilder>();

        public TableAnnotationToSqlBuilder(T t) throws DboValidityException {

            tableName = getTableNameFromAnnotation(t);

            if (!hasContent(tableName)) {
                throw new DboValidityException("Type T has no @Table annotation.");
            }

            fieldAnnotationToSqlBuilders = getFieldAnnotationToSqlBuilders(t);

            if (!hasContent(fieldAnnotationToSqlBuilders)) {
                throw new DboValidityException("Type T has no field annotations.");
            }

            String fieldsSql = buildFieldsSql(fieldAnnotationToSqlBuilders);

            // TOIMPROVE: manual id fields, setting primary key
            createTableSql = format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %sPRIMARY KEY(id));", tableName, fieldsSql);

            dropTableSql = format("DROP TABLE IF EXISTS %s;", tableName);
        }

        private String buildFieldsSql(List<FieldAnnotationToSqlBuilder> fieldAnnotationToSqlBuilders) {
            String sql = "";
            for (FieldAnnotationToSqlBuilder fieldBuilder : fieldAnnotationToSqlBuilders) {
                sql += fieldBuilder.getFieldSql();
            }
            return sql;
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
            for (Field field : t.getClass().getDeclaredFields()) {

                // TOIMPROVE: extract the sql builders to their own
                FieldAnnotationToSqlBuilder fieldToSqlBuilder = new FieldAnnotationToSqlBuilder(field);

                if (fieldToSqlBuilder.isDboField()) {
                    fieldAnnotationToSqlBuilders.add(fieldToSqlBuilder);
                }
            }

            return fieldAnnotationToSqlBuilders;
        }

        public String getCreateSql() {
            return createTableSql;
        }

        public String getDropSql() {
            return dropTableSql;
        }
    }

    private class FieldAnnotationToSqlBuilder {

        private String sql = "";
        private String fieldName = "";
        private String fieldLength;
        private boolean notNull = true;
        private FieldType fieldType;

        public FieldAnnotationToSqlBuilder(Field declaredField) {

            fieldName = declaredField.getName();
            Annotation[] annotations = declaredField.getAnnotations();

            for (Annotation annotation : annotations) {

                if (annotation instanceof VarChar) {
                    VarChar varChar = (VarChar) annotation;
                    fieldType = FieldType.VARCHAR;
                    fieldLength = varChar.length();
                    notNull = varChar.notNull();

                    // email VARCHAR (40) NOT NULL,
                    sql = format("%s VARCHAR(%s) %s, ", fieldName, fieldLength, (notNull ? "NOT NULL" : ""));
                    break;

                } else {

                    fieldType = FieldType.NON_DBO_FIELD;
                }

                // TOIMPROVE: expand field coverage: ints, dates, etc
            }
        }

        public boolean isDboField() {
            return !FieldType.NON_DBO_FIELD.equals(fieldType);
        }

        public String getFieldSql() {
            return sql;
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
