package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.hasContent;
import static mg.util.validation.Validator.validateNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.FieldBuilderFactory;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.db.persist.field.IdBuilder;
import mg.util.functional.function.ThrowingFunction;

class SqlBuilder {

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

    private AliasBuilder aliasBuilder = new AliasBuilder();
    private List<FieldBuilder> collectionBuilders;
    private List<ConstraintBuilder> constraints;
    private List<FieldBuilder> fieldBuilders;
    private List<FieldBuilder> foreignKeyBuilders;
    private List<FieldBuilder> idBuilders;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private FieldBuilder primaryKeyBuilder;
    private String tableName;
    private Persistable type;

    public <T extends Persistable> SqlBuilder(T t) throws DBValidityException {

        type = validateNotNull("t", t);

        tableName = getTableNameAndValidate(t);
        List<FieldBuilder> allBuilders = getAllBuilders(t);
        fieldBuilders = getFieldBuildersAndValidate(allBuilders);
        idBuilders = getIdBuildersAndValidate(allBuilders);
        primaryKeyBuilder = getPrimaryKeyBuilder(idBuilders);
        foreignKeyBuilders = getForeignKeyBuilders(allBuilders);
        collectionBuilders = getCollectionBuilders(allBuilders);
        constraints = t.getConstraints();
    }

    public String buildCreateTable() {
        String fields = fieldBuilders.stream()
                                     .map(fb -> fb.build())
                                     .collect(Collectors.joining(", "));

        String primaryKey = fieldBuilders.stream()
                                         .filter(fb -> fb.isIdField())
                                         .map(fb -> fb.getName())
                                         .filter(s -> hasContent(s))
                                         .collect(Collectors.joining(", "));

        String foreignKey = foreignKeyBuilders.stream()
                                              .map(fb -> fb.buildForeignKey())
                                              .filter(s -> hasContent(s))
                                              .collect(Collectors.joining(","));

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
          .append(tableName)
          .append(" (")
          .append(fields)
          .append(hasContent(primaryKey) ? ", PRIMARY KEY (" + primaryKey + ")" : "")
          .append(hasContent(foreignKey) ? ", " + foreignKey : "")
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

    // reference pair: tableName.field, tableName.field
    public Map<String, List<FieldReference>> buildReferences(List<SqlBuilder> sqlBuilders) {

        // List<FieldReference> references = new ArrayList<>();
        Map<String, List<FieldReference>> references = new LinkedHashMap<>();

        // while loop since .stream().windowed(2) || .sliding(2) is missing, TOCONSIDER: write a windowed processor (spliterator? iterator?)
        if (sqlBuilders.size() > 1) {
            Iterator<SqlBuilder> sqlBuilderIterator = sqlBuilders.iterator();
            SqlBuilder left = null;
            SqlBuilder right = sqlBuilderIterator.next(); // sliding(2) || windowed(2)
            while (sqlBuilderIterator.hasNext()) {
                left = right;
                right = sqlBuilderIterator.next();

                references.put(left.getTableName(), getReferences(left, right));
            }
        }

        return references;
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

        String ids = fieldBuilders.stream()
                                  .filter(fb -> fb.isIdField())
                                  .map(fb -> fb.getName() + " = " + fb.getValue())
                                  .collect(Collectors.joining(", "));

        String tableNameAlias = aliasBuilder.aliasOf(tableName);

        String fieldNames = buildFieldNames(fieldBuilders, tableNameAlias);

        return new StringBuilder("SELECT ").append(fieldNames)
                                           .append(" FROM ")
                                           .append(tableName)
                                           .append(" AS ")
                                           .append(tableNameAlias)
                                           .append(" WHERE ")
                                           .append(ids)
                                           .append(";")
                                           .toString();
    }

    public String buildUpdate() {
        String fields = fieldBuilders.stream()
                                     .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                     .map(fieldBuilder -> fieldBuilder.getName() + " = ?")
                                     .collect(Collectors.joining(", "));

        return format("UPDATE %s SET %s;", tableName, fields);
    }

    public AliasBuilder getAliasBuilder() {
        return aliasBuilder;
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

    public FieldBuilder getPrimaryKeyBuilder() {
        return primaryKeyBuilder;
    }

    // TOIMPROVE: clarity, brevity, meaning of naming
    // TOIMPROVE: add OneToOne, OneToMany
    public Stream<Persistable> getReferencePersistables() throws DBValidityException {

        List<FieldBuilder> colBuilders = getCollectionBuilders();

        return colBuilders.stream()
                          .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                          .filter(object -> object instanceof Persistable)
                          .map(object -> (Persistable) object);
    }

    public List<SqlBuilder> getSqlBuilders(List<Persistable> uniquePersistables) {
        return uniquePersistables.stream()
                                 .map((ThrowingFunction<Persistable, SqlBuilder, Exception>) p -> SqlBuilder.of(p))
                                 .collect(Collectors.toList());
    }

    public String getTableName() {
        return tableName;
    }

    public Collection<Persistable> getUniquePersistables(List<FieldBuilder> collectionBuilders) {
        Collection<Persistable> uniquePersistables;
        uniquePersistables = collectionBuilders.stream()
                                               .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                                               .filter(object -> object instanceof Persistable)
                                               .map(object -> (Persistable) object)
                                               .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p)) // this here uses the key as Object.class -> no duplicates
                                               .values();
        return uniquePersistables;
    }

    public Stream<Persistable> getUniquePersistablesCascading(Persistable persistable) throws DBValidityException {

        SqlBuilder sqlBuilder = SqlBuilder.of(persistable);

        Stream<Persistable> uniqueCollectionPersistables;
        uniqueCollectionPersistables = sqlBuilder.getCollectionBuilders()
                                                 .stream()
                                                 .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getValue()))
                                                 .filter(object -> object instanceof Persistable)
                                                 .map(object -> (Persistable) object)
                                                 .flatMap((ThrowingFunction<Persistable, Stream<Persistable>, Exception>) subPersistable -> {
                                                     return getUniquePersistablesCascading(subPersistable);
                                                 });

        return Stream.concat(Stream.of(persistable), uniqueCollectionPersistables);
    }

    /**
     * Refreshes every IdBuilder from the reflected fields.
     */
    public void refreshIdBuilders() {
        fieldBuilders.stream()
                     .filter(fb -> fb.isIdField())
                     .forEach(fb -> fb.refresh());
    }

    private String buildConstraints(List<SqlBuilder> sqlBuilders) {
        return sqlBuilders.stream()
                          .filter(sb -> !sb.getConstraints().isEmpty())
                          .map(sb -> sb.buildConstraints(aliasBuilder.aliasOf(sb.getTableName()),
                                                         sb.getConstraints()))
                          .collect(Collectors.joining(" AND "));
    }

    private String buildConstraints(String tableNameAlias, List<ConstraintBuilder> constraints) {
        return constraints.stream()
                          .map(concstraintBuilder -> tableNameAlias + "." + concstraintBuilder.build())
                          .collect(Collectors.joining(" AND "));
    }

    private String buildFieldNames(List<FieldBuilder> fieldBuilders, String tableNameAlias) {
        return fieldBuilders.stream()
                            .map(fb -> tableNameAlias + "." + fb.getName())
                            .collect(Collectors.joining(", "));
    }

    private String buildFieldNames(List<SqlBuilder> sqlBuilders) {

        return sqlBuilders.stream()
                          .map(sb -> {
                              List<FieldBuilder> fieldBuilders = sb.getFieldBuilders();
                              String tableNameAlias = aliasBuilder.aliasOf(sb.getTableName());
                              return buildFieldNames(fieldBuilders, tableNameAlias);
                          })
                          .collect(Collectors.joining(", "));
    }

    private String buildJoins(Map<String, List<FieldReference>> references) {
        return references.entrySet()
                         .stream()
                         .flatMap(entry -> entry.getValue().stream())
                         .map(ref -> {
                             StringBuilder sb = new StringBuilder("JOIN ");
                             String referringTableAlias = aliasBuilder.aliasOf(ref.referringTable);
                             String referredTableAlias = aliasBuilder.aliasOf(ref.referredTable);
                             return sb.append(ref.referringTable)
                                      .append(" AS ")
                                      .append(referringTableAlias)
                                      .append(" ON ")
                                      .append(referredTableAlias)
                                      .append(".")
                                      .append(ref.referredField.getName())
                                      .append(" = ")
                                      .append(referringTableAlias)
                                      .append(".")
                                      .append(ref.referringField.getName())
                                      .toString();
                         })
                         .collect(Collectors.joining(" "));
    }

    private String buildSelectByFieldsCascading() throws DBValidityException {

        // TODO: buildSelectByFieldsCascading: cases: OneToMany, OneToOne
        List<Persistable> uniquePersistables = getUniquePersistablesCascading(type).collect(Collectors.toList());

        List<SqlBuilder> sqlBuilders = getSqlBuilders(uniquePersistables);

        Map<String, List<FieldReference>> references = buildReferences(sqlBuilders);

        String joins = buildJoins(references);

        String constraints = buildConstraints(sqlBuilders);

        String tableNameAlias = aliasBuilder.aliasOf(tableName);

        String fieldNames = buildFieldNames(sqlBuilders);

        StringBuilder byFieldsSql = new StringBuilder("SELECT ");

        byFieldsSql.append(fieldNames)
                   .append(" FROM ")
                   .append(tableName)
                   .append(" AS ")
                   .append(tableNameAlias)
                   .append(hasContent(joins) ? " " + joins : "")
                   .append(" WHERE ")
                   .append(constraints)
                   .append(";");

        logger.debug("SQL by fields: " + byFieldsSql);
        return byFieldsSql.toString();

    }

    private String buildSelectByFieldsSingular() throws DBValidityException {

        String tableNameAlias = aliasBuilder.aliasOf(tableName);

        String fieldNames = buildFieldNames(fieldBuilders, tableNameAlias);

        String constraintsString = buildConstraints(tableNameAlias, constraints);

        StringBuilder byFields = new StringBuilder("SELECT ").append(fieldNames)
                                                             .append(" FROM ")
                                                             .append(tableName)
                                                             .append(" AS ")
                                                             .append(tableNameAlias)
                                                             .append(" WHERE ")
                                                             .append(constraintsString)
                                                             .append(";");

        logger.debug("SQL by fields: " + byFields);

        return byFields.toString();
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

    private FieldBuilder getPrimaryKeyBuilder(List<FieldBuilder> idBuilders) {

        return idBuilders.stream()
                         .filter(idBuilder -> idBuilder.isPrimaryKeyField())
                         .findFirst()
                         .get();
    }

    // TOCONSIDER: change to SqlBuilder left, SqlBuilder right, get refs, swap them and get refs again, return as list.
    private List<FieldReference> getReferences(SqlBuilder referredBuilder, SqlBuilder referringBuilder) {

        List<FieldBuilder> referring = referringBuilder.getForeignKeyBuilders();
        List<FieldBuilder> referred = referredBuilder.getFieldBuilders();

        return referring.stream()
                        .filter(fk -> fk instanceof ForeignKeyBuilder)
                        .map(fk -> (ForeignKeyBuilder) fk)
                        .flatMap(fk -> {
                            return referred.stream()
                                           .filter(fb -> referredBuilder.getTableName().equals(fk.getReferences()) &&
                                                         fb.getName().equals(fk.getField()))
                                           .map(fb -> new FieldReference(referredBuilder.getTableName(),
                                                                         fb,
                                                                         referringBuilder.getTableName(),
                                                                         fk));
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
