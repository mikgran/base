package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.hasContent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;

class TableBuilder {

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

    public String buildCreateSql() {
        String fieldsSql = fieldBuilders.stream()
                                        .map(fieldBuilder -> fieldBuilder.getSql())
                                        .collect(Collectors.joining(", "));

        return format("CREATE TABLE IF NOT EXISTS %s (id MEDIUMINT NOT NULL AUTO_INCREMENT, %s, PRIMARY KEY(id));", tableName, fieldsSql);
    }

    public String buildDropSql() {
        return format("DROP TABLE IF EXISTS %s;", tableName);
    }

    // TOIMPROVE: partial updates
    public String buildInsertSql() {
        String sqlColumns = fieldBuilders.stream()
                                         .map(fieldBuilder -> fieldBuilder.getName())
                                         .collect(Collectors.joining(", "));

        String questionMarks = fieldBuilders.stream()
                                            .map(fieldBuilder -> "?")
                                            .collect(Collectors.joining(", "));

        return format("INSERT INTO %s (%s) VALUES(%s);", tableName, sqlColumns, questionMarks);
    }

    public String buildRemoveSql() {
        return format("DELETE FROM %s WHERE id = %s;", tableName, id);
    }

    public String buildSelectBySql() {
        // XXX: add fetch by <field>
        // <T t, V v> findBy(T t, V v, Function<T> left, Function<V> right)
        // <T t> findBy()
        return "";
    }

    public String buildUpdateSql() {
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

    private <T extends Persistable> String getTableNameAndValidate(T t) throws DBValidityException {

        Table tableAnnotation = t.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new DBValidityException("Type T has no @Table annotation.");
        }

        return tableAnnotation.name();
    }

}