package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.hasContent;
import static mg.util.validation.Validator.validateNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;

// TOIMPROVE: use table.field names in building.
class SqlBuilder {

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

    private List<FieldBuilder> collectionBuilders;
    private List<ConstraintBuilder> constraints;
    private List<FieldBuilder> fieldBuilders;
    private List<FieldBuilder> foreignKeyBuilders;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String tableName;

    public <T extends Persistable> SqlBuilder(T t) throws DBValidityException {

        validateNotNull("t", t);

        tableName = getTableNameAndValidate(t);
        List<FieldBuilder> allBuilders = getAllBuilders(t);
        fieldBuilders = getFieldBuildersAndValidate(allBuilders);
        foreignKeyBuilders = getForeignKeyBuilders(allBuilders);
        collectionBuilders = getCollectionBuilders(allBuilders);
        constraints = t.getConstraints();
    }

    public String buildCreateTable() {
        String fieldsSql = fieldBuilders.stream()
                                        .map(FieldBuilder::build)
                                        .collect(Collectors.joining(", "));

        String foreignSql = foreignKeyBuilders.stream()
                                              .map(FieldBuilder::buildForeignKey)
                                              .collect(Collectors.joining(","));

        if (hasContent(foreignSql)) {
            foreignSql = ", " + foreignSql;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
          .append(tableName)
          .append(" (")
          .append(fieldsSql)
          .append(foreignSql)
          .append(");");

        return sb.toString();
    }

    public String buildDelete() {
        String idsNamesValues = fieldBuilders.stream()
                                             .filter(fb -> fb.isIdField())
                                             .map(fb -> fb.getName() + " = " + fb.getValue())
                                             .collect(Collectors.joining(", "));

        return new StringBuilder("DELETE FROM ").append(tableName)
                                                .append(" WHERE ")
                                                .append(idsNamesValues)
                                                .append(";")
                                                .toString();
    }

    public String buildDropTable() {
        return new StringBuilder("DROP TABLE IF EXISTS ").append(tableName)
                                                         .append(";")
                                                         .toString();
    }

    // TOIMPROVE: partial updates
    public String buildInsert() {

        String sqlColumns = fieldBuilders.stream()
                                         .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                         .map(fieldBuilder -> fieldBuilder.getName())
                                         .collect(Collectors.joining(", "));

        String questionMarks = fieldBuilders.stream()
                                            .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                            .map(fieldBuilder -> "?")
                                            .collect(Collectors.joining(", "));

        return new StringBuilder("INSERT INTO ").append(tableName)
                                                .append(" (")
                                                .append(sqlColumns)
                                                .append(") VALUES(")
                                                .append(questionMarks)
                                                .append(");")
                                                .toString();
    }

    public String buildSelectByFields() throws DBValidityException {

        // TOIMPROVE: join by collection constraints
        // TOIMPROVE: use table names in selects - avoids column name collisions
        if (constraints.size() == 0) {
            throw new DBValidityException("No constraints to build from: expecting at least one field constraint.");
        }

        StringBuilder byFieldsSql = new StringBuilder("SELECT * FROM ").append(tableName)
                                                                       .append(" WHERE ");

        String constraintsString = constraints.stream()
                                              .map(ConstraintBuilder::build)
                                              .collect(Collectors.joining(" AND "));

        byFieldsSql.append(constraintsString)
                   .append(";");

        logger.debug("SQL by fields: " + byFieldsSql);

        return byFieldsSql.toString();
    }

    public String buildSelectByIds() {
        // TOIMPROVE: instead build a fieldBuilders.get(x).getName() based solution
        // -> alter tables would be less likely to crash the select and resultset mapping:
        // columns and fields type and or count mismatch after alter table
        String ids = fieldBuilders.stream()
                                  .filter(fb -> fb.isIdField())
                                  .map(fb -> fb.getName() + " = " + fb.getValue())
                                  .collect(Collectors.joining(", "));

        return new StringBuilder("SELECT * FROM ").append(tableName)
                                                  .append(" WHERE ")
                                                  .append(ids)
                                                  .append(";")
                                                  .toString();
    }

    public String buildUpdate() {
        String fieldsSql = fieldBuilders.stream()
                                        .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                        .map(fieldBuilder -> fieldBuilder.getName() + " = ?")
                                        .collect(Collectors.joining(", "));

        return format("UPDATE %s SET %s;", tableName, fieldsSql);
    }

    public List<FieldBuilder> getCollectionBuilders() {
        return collectionBuilders;
    }

    public List<ConstraintBuilder> getConstraints() {
        return constraints;
    }

    public List<FieldBuilder> getFieldBuilders() {
        return fieldBuilders;
    }

    public List<FieldBuilder> getForeignKeyBuilders() {
        return foreignKeyBuilders;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * Refreshes every IdBuilder from the reflected fields.
     */
    public void refreshIdBuilders() {
        fieldBuilders.stream()
                     .filter(fb -> fb.isIdField())
                     .forEach(fb -> fb.refresh());
    }

    private <T extends Persistable> List<FieldBuilder> getAllBuilders(T t) {
        return Arrays.stream(t.getClass().getDeclaredFields())
                     .map(declaredField -> FieldBuilderFactory.of(t, declaredField))
                     .collect(Collectors.toList());
    }

    private <T extends Persistable> List<FieldBuilder> getCollectionBuilders(List<FieldBuilder> allBuilders) {
        return allBuilders.stream()
                          .filter(fieldBuilder -> fieldBuilder.isCollectionField())
                          .collect(Collectors.toList());
    }

    private <T extends Persistable> List<FieldBuilder> getFieldBuildersAndValidate(List<FieldBuilder> allBuilders) throws DBValidityException {

        List<FieldBuilder> fieldBuilders = allBuilders.stream()
                                                      .filter(fieldBuilder -> fieldBuilder.isDbField())
                                                      .collect(Collectors.toList());

        if (!hasContent(fieldBuilders)) {
            throw new DBValidityException("Type T has no field annotations.");
        }

        return fieldBuilders;
    }

    private <T extends Persistable> List<FieldBuilder> getForeignKeyBuilders(List<FieldBuilder> allBuilders) {
        return allBuilders.stream()
                          .filter(fieldBuilder -> fieldBuilder.isForeignKeyField())
                          .collect(Collectors.toList());
    }

    private <T extends Persistable> String getTableNameAndValidate(T t) throws DBValidityException {

        Table tableAnnotation = t.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new DBValidityException("Type T has no @Table annotation.");
        }

        return tableAnnotation.name();
    }

}
