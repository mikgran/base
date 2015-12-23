package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.hasContent;
import static mg.util.validation.Validator.validateNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;

class SqlBuilder {

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }
    private List<FieldBuilder> collectionBuilders;
    private List<FieldBuilder> fieldBuilders;
    private int id = 0;

    private String tableName;

    public <T extends Persistable> SqlBuilder(T t) throws DBValidityException {

        validateNotNull("t", t);

        tableName = getTableNameAndValidate(t);
        fieldBuilders = getFieldBuildersAndValidate(t);
        collectionBuilders = getCollectionBuilders(t);
        id = t.getId();
    }

    public String buildCreateTable() {
        String fieldsSql = fieldBuilders.stream()
                                        .map(fieldBuilder -> fieldBuilder.getSql())
                                        .collect(Collectors.joining(", "));

        return format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %s, PRIMARY KEY(id));", tableName, fieldsSql);
    }

    public String buildDelete() {
        return format("DELETE FROM %s WHERE id = %s;", tableName, id);
    }

    public String buildDropTable() {
        return format("DROP TABLE IF EXISTS %s;", tableName);
    }

    // TOIMPROVE: partial updates
    public String buildInsert() {
        String sqlColumns = fieldBuilders.stream()
                                         .map(fieldBuilder -> fieldBuilder.getName())
                                         .collect(Collectors.joining(", "));

        String questionMarks = fieldBuilders.stream()
                                            .map(fieldBuilder -> "?")
                                            .collect(Collectors.joining(", "));

        return format("INSERT INTO %s (%s) VALUES(%s);", tableName, sqlColumns, questionMarks);
    }

    public String buildSelectById() {
        // TOIMPROVE: instead build a fieldBuilders.get(x).getName() based solution
        // -> alter tables would be less likely to crash the select and resultset mapping:
        // columns and fields type and or count mismatch after alter table
        return format("SELECT * FROM %s WHERE id = %s;", tableName, id);
    }

    public String buildUpdate() {
        String fieldsSql = fieldBuilders.stream()
                                        .map(fieldBuilder -> fieldBuilder.getName() + " = ?")
                                        .collect(Collectors.joining(", "));

        return format("UPDATE %s SET %s;", tableName, fieldsSql);
    }

    public List<FieldBuilder> getCollectionBuilders() {
        return collectionBuilders;
    }

    public List<FieldBuilder> getFieldBuilders() {
        return fieldBuilders;
    }

    public String getTableName() {
        return tableName;
    }

    private <T extends Persistable> List<FieldBuilder> getCollectionBuilders(T t) {
        return Arrays.stream(t.getClass().getDeclaredFields())
                     .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                     .filter(fieldBuilder -> fieldBuilder.isCollectionField())
                     .collect(Collectors.toList());
    }

    private <T extends Persistable> List<FieldBuilder> getFieldBuildersAndValidate(T t) throws DBValidityException {

        List<FieldBuilder> fieldBuilders;
        fieldBuilders = Arrays.stream(t.getClass().getDeclaredFields())
                              .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                              .filter(fieldBuilder -> fieldBuilder.isDbField())
                              .collect(Collectors.toList());

        if (!hasContent(fieldBuilders)) {
            throw new DBValidityException("Type T has no field annotations.");
        }

        return fieldBuilders;
    }

    private <T extends Persistable> String getTableNameAndValidate(T t) throws DBValidityException {

        Table tableAnnotation = t.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new DBValidityException("Type T has no @Table annotation.");
        }

        return tableAnnotation.name();
    }

}
