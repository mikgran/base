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

import mg.util.db.dbo.annotation.Table;
import mg.util.db.dbo.annotation.VarChar;
import mg.util.validation.Validator;

/**
 * Conveniency class to create and drop a table of Type T and to persist or
 * remove a row corresponding this object.
 *
 * @param <T>
 *            Type T object used to persist, remove, create a table or remove a
 *            table.
 */
public class Dbo<T> {

    private T t;
    private Connection connection;

    private String name = "";
    private String id = "";
    private List<ClassFieldAnnotation> fields = new ArrayList<ClassFieldAnnotation>();

    @SuppressWarnings("unused")
    private Dbo() {
    }

    /**
     * Constructs the DBO<?>.
     * 
     * @param connection
     *            open database connection.
     * @param t
     *            Type T object that has been annotated with @Table.
     * @throws DboValidityException
     *             if @Table and atleast one field annotation was missing from
     *             Type T or there was an sql exception.
     * 
     * @throws InvalidArgumentException
     *             in the case that connection or t was a null.
     */
    public Dbo(Connection connection, T t) throws DboValidityException {

        new Validator().add("connection", connection, NOT_NULL)
                       .add("t", t, NOT_NULL)
                       .validate();

        this.t = t;
        this.connection = connection;

        name = getTableAnnotation(t);
        id = getIdAnnotation(t);
        fields = getClassFieldsAnnotations();

        validateRequiredAnnotationData(name, fields);
    }

    /**
     * Creates a table from Type T using annotation @Table(name="tableName") and
     * fields annotated with @VarChar or other field annotations. See
     * mg.util.db.dbo.annotation package.
     * 
     * @throws SQLException
     *             if any database error occured. For instance: user tried to
     *             apply @VarChar to an int field.
     */
    public void createTable() throws DboValidityException, SQLException {

        String sql = getSqlForCreateTable(name, id, fields);

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private String getSqlForCreateTable(String name, String id, List<ClassFieldAnnotation> fields) {

        String sqlForFields = "";
        for (ClassFieldAnnotation field : fields) {
            // email VARCHAR(40) NOT NULL,
            sqlForFields += format("%s %s(%s) NOT NULL, ", field.getFieldName(), field.getType().toString(), field.getFieldLength());
        }

        return format("CREATE TABLE %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %sPRIMARY KEY(id));", name, sqlForFields);
    }

    private void validateRequiredAnnotationData(String name, List<ClassFieldAnnotation> fields) throws DboValidityException {

        if (!hasContent(name)) {
            throw new DboValidityException("Type T has no @Table annotation.");
        }

        if (!hasContent(fields)) {
            throw new DboValidityException("Type T has no field annotations.");
        }

    }

    private List<ClassFieldAnnotation> getClassFieldsAnnotations() {

        Field[] declaredFields = t.getClass().getDeclaredFields();
        List<ClassFieldAnnotation> classFields = new ArrayList<ClassFieldAnnotation>();

        for (Field field : declaredFields) {

            String fieldName = field.getName();

            Annotation[] declaredAnnotations = field.getDeclaredAnnotations();

            // TOIMPROVE: handle multiple annotations and their error situations
            for (Annotation annotation : declaredAnnotations) {

                if (annotation instanceof VarChar) {
                    VarChar varChar = (VarChar) annotation;

                    // fields.add(format("%s VARCHAR(%s)", fieldName,
                    // varChar.length()));
                    classFields.add(new ClassFieldAnnotation(FieldType.VARCHAR, fieldName, varChar.length()));
                    break;
                }

                // elseif () {}
                // TOIMPROVE other field annotations like INT / MEDIUMINT /
                // LONGINT / dates etc
            }
        }

        return classFields;
    }

    private String getIdAnnotation(T t) {

        // TOIMPROVE: hadle manual IDs in the future.

        return null;
    }

    private String getTableAnnotation(T t) {

        Annotation[] classAnnotations = t.getClass().getAnnotations();

        for (Annotation annotation : classAnnotations) {

            if (annotation instanceof Table) {

                return ((Table) annotation).name();
            }
        }

        return null;
    }

    public void dropTable() throws SQLException {

        String tableAnnotation = getTableAnnotation(t);
        String sql = format("DROP TABLE IF EXISTS %s;", tableAnnotation);

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private enum FieldType {
        ID("ID"), INT("INT"), VARCHAR("VARCHAR");
        private final String str;
        FieldType(String str) {
            this.str = str;
        }
        public String toString() {
            return str;
        }
    }

    private class ClassFieldAnnotation {

        private String length;
        private String name;
        private FieldType type;

        public ClassFieldAnnotation(FieldType type, String name, String length) {
            this.type = type;
            this.name = name;
            this.length = length;
        }

        public String getFieldLength() {
            return length;
        }

        public String getFieldName() {
            return name;
        }

        public FieldType getType() {
            return type;
        }
    }

}
