package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.hasContent;
import static mg.util.Common.unwrapCauseAndRethrow;
import static mg.util.validation.Validator.validateNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.Tuple4;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.db.persist.field.IdBuilder;
import mg.util.functional.function.ThrowingFunction;

// TOIMPROVE: use table.field names in building.
class SqlBuilder {

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

    private List<FieldBuilder> collectionBuilders;
    private List<ConstraintBuilder> constraints;
    private List<FieldBuilder> fieldBuilders;
    private List<FieldBuilder> foreignKeyBuilders;
    private List<FieldBuilder> idBuilders;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String tableName;
    public <T extends Persistable> SqlBuilder(T t) throws DBValidityException {

        validateNotNull("t", t);

        tableName = getTableNameAndValidate(t);
        List<FieldBuilder> allBuilders = getAllBuilders(t);
        fieldBuilders = getFieldBuildersAndValidate(allBuilders);
        idBuilders = getIdBuildersAndValidate(allBuilders);
        foreignKeyBuilders = getForeignKeyBuilders(allBuilders);
        collectionBuilders = getCollectionBuilders(allBuilders);
        constraints = t.getConstraints();
    }

    public String buildCreateTable() {
        String fieldsSql = fieldBuilders.stream()
                                        .map(fb -> fb.build())
                                        .collect(Collectors.joining(", "));

        String primaryKeySql = fieldBuilders.stream()
                                            .filter(fb -> fb.isIdField())
                                            .map(fb -> fb.getName())
                                            .filter(s -> hasContent(s))
                                            .collect(Collectors.joining(", "));

        String foreignKeySql = foreignKeyBuilders.stream()
                                                 .map(fb -> fb.buildForeignKey())
                                                 .filter(s -> hasContent(s))
                                                 .collect(Collectors.joining(","));

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
          .append(tableName)
          .append(" (")
          .append(fieldsSql)
          .append(hasContent(primaryKeySql) ? ", PRIMARY KEY (" + primaryKeySql + ")" : "")
          .append(hasContent(foreignKeySql) ? ", " + foreignKeySql : "")
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

        if (constraints.size() == 0) {
            throw new DBValidityException("No constraints to build from: expecting at least one field constraint for table: " + tableName);
        }

        // TODO: buildSelectByFields: OneToMany, OneToOne relations
        if (collectionBuilders.size() > 0) {

            return buildSelectByFieldsCascading();
        } else {

            return buildSelectByFieldsSingular();
        }
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

    public List<FieldBuilder> getIdBuilders() {
        return idBuilders;
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

    private String buildConstraints(String tableName, List<ConstraintBuilder> constraints) {
        return constraints.stream()
                          .map(concstraintBuilder -> tableName + "." + concstraintBuilder.build())
                          .collect(Collectors.joining(" AND "));
    }

    // reference pair: tableName.field, tableName.field
    private List<Tuple4<String, String, String, String>> buildReferenceTuples(List<SqlBuilder> sqlBuilders) {

        List<Tuple4<String, String, String, String>> references = new ArrayList<>();

        try {
            // while loop since .stream().windowed(2) || .sliding(2) is missing, TOCONSIDER: write a windowed processor (spliterator? iterator?)
            if (sqlBuilders.size() > 1) {
                Iterator<SqlBuilder> sqlBuilderIterator = sqlBuilders.iterator();
                SqlBuilder left = null;
                SqlBuilder right = sqlBuilderIterator.next(); // sliding(2) || windowed(2)
                while (sqlBuilderIterator.hasNext()) {
                    left = right;
                    right = sqlBuilderIterator.next();

                    references.addAll(getReferences(left, right));
                }
            }

        } catch (RuntimeException e) {
            unwrapCauseAndRethrow(e);
        }

        return references;
    }

    private String buildSelectByFieldsCascading() throws DBValidityException {

        // TODO: buildSelectByFieldsCascading: cases: OneToMany, OneToOne
        // find referencing pairs Person.getTodos(): persons.id <- todos.personid, Todo.getLocations(): todos.id <- locations.todosId
        // build joins from the pairs
        // fill up WHERE table.field = narrow partials

        Collection<Persistable> uniquePersistables;
        uniquePersistables = collectionBuilders.stream()
                                               .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                                               .filter(object -> object instanceof Persistable)
                                               .map(object -> (Persistable) object)
                                               .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p)) // this here uses the key as Object.class -> no duplicates
                                               .values();

        List<SqlBuilder> sqlBuilders;
        sqlBuilders = uniquePersistables.stream()
                                        .map((ThrowingFunction<Persistable, SqlBuilder, Exception>) p -> SqlBuilder.of(p))
                                        .collect(Collectors.toList());
        sqlBuilders.add(0, this);

        List<Tuple4<String, String, String, String>> referenceTuples;
        referenceTuples = buildReferenceTuples(sqlBuilders);

        String joins;
        joins = referenceTuples.stream()
                               .map(tuple -> {
                                   StringBuilder sb = new StringBuilder("JOIN ");
                                   return sb.append(tuple._3)
                                            .append(" ON ")
                                            .append(tuple._1)
                                            .append(".")
                                            .append(tuple._2)
                                            .append(" = ")
                                            .append(tuple._3)
                                            .append(".")
                                            .append(tuple._4)
                                            .toString();
                               })
                               .collect(Collectors.joining(", "));

        String constraintsString = sqlBuilders.stream()
                                              .map(sb -> sb.buildConstraints(sb.getTableName(),
                                                                             sb.getConstraints()))
                                              .collect(Collectors.joining(" AND "));

        // String constraintsString = buildConstraints(tableName, constraints);

        StringBuilder byFieldsSql;
        byFieldsSql = new StringBuilder("SELECT * FROM ").append(tableName)
                                                         .append(hasContent(joins) ? " " + joins : "")
                                                         .append(" WHERE ")
                                                         .append(constraintsString)
                                                         .append(";");

        logger.debug("SQL by fields: " + byFieldsSql);

        return byFieldsSql.toString();

    }

    private String buildSelectByFieldsSingular() throws DBValidityException {
        // TOIMPROVE: use table names in selects - avoids column name collisions

        String constraintsString = buildConstraints(tableName, constraints);

        StringBuilder byFieldsSql = new StringBuilder("SELECT * FROM ").append(tableName)
                                                                       .append(" WHERE ")
                                                                       .append(constraintsString)
                                                                       .append(";");

        logger.debug("SQL by fields: " + byFieldsSql);

        return byFieldsSql.toString();
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

    private List<FieldBuilder> getIdBuildersAndValidate(List<FieldBuilder> allBuilders) throws DBValidityException {

        List<FieldBuilder> idBuilders = allBuilders.stream()
                                                   .filter(fb -> fb.isIdField())
                                                   .collect(Collectors.toList());

        List<IdBuilder> ids = idBuilders.stream()
                                        .map(ib -> (IdBuilder) ib)
                                        .collect(Collectors.toList());

        long autoIncrementFieldCount = ids.stream().filter(id -> id.isAutoIncrement()).count();
        long primaryKeyFieldCount = ids.stream().filter(id -> id.isPrimaryKeyField()).count();

        if (autoIncrementFieldCount > 1) {
            throw new DBValidityException("Type T can not contain more than one id field with autoIncrement.");
        }

        if (primaryKeyFieldCount < 1) {
            throw new DBValidityException("Type T does not contain an expected primary key field.");
        }

        return idBuilders;
    }

    // TOCONSIDER: change to SqlBuilder left, SqlBuilder right, get refs, swap them and get refs again, return as list.
    private List<Tuple4<String, String, String, String>> getReferences(SqlBuilder referredBuilder, SqlBuilder referringBuilder) {

        List<FieldBuilder> referring = referringBuilder.getForeignKeyBuilders();
        List<FieldBuilder> referred = referredBuilder.getFieldBuilders();

        return referring.stream()
                        .filter(fk -> fk instanceof ForeignKeyBuilder)
                        .map(fk -> (ForeignKeyBuilder) fk)
                        .flatMap(fk -> {
                            return referred.stream()
                                           .filter(fb -> referredBuilder.getTableName().equals(fk.getReferences()) &&
                                                         fb.getName().equals(fk.getField()))
                                           .map(fb -> new Tuple4<>(referredBuilder.getTableName(),
                                                                   fb.getName(),
                                                                   referringBuilder.getTableName(),
                                                                   fk.getName()));
                        })
                        .collect(Collectors.toList());
    }

    private <T extends Persistable> String getTableNameAndValidate(T t) throws DBValidityException {

        Table tableAnnotation = t.getClass().getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new DBValidityException("Type T has no @Table annotation.");
        }

        return tableAnnotation.name();
    }

    @SuppressWarnings("unused")
    private boolean hasReference(SqlBuilder referredBuilder, SqlBuilder referringBuilder) {

        List<FieldBuilder> referring = referringBuilder.getForeignKeyBuilders();
        List<FieldBuilder> referred = referredBuilder.getFieldBuilders();

        return referring.stream()
                        .filter(fk -> fk instanceof ForeignKeyBuilder)
                        .map(fk -> (ForeignKeyBuilder) fk)
                        .filter(fk -> {
                            return referred.stream()
                                           .filter(fb -> referredBuilder.getTableName().equals(fk.getReferences()) &&
                                                         fb.getName().equals(fk.getField()))
                                           .findFirst()
                                           .isPresent();
                        })
                        .findFirst()
                        .isPresent();
    }

}
