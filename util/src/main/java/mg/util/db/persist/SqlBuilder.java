package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.hasContent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.db.persist.support.Person4;
import mg.util.functional.function.ThrowingFunction;

class SqlBuilder {

    public enum JoinPolicy {
        JOIN("JOIN "), LEFT_JOIN("LEFT JOIN ");

        private final String policy;

        private JoinPolicy(String policy) {
            this.policy = policy;
        }

        public boolean equalsPolicy(String other) {
            return (other == null) ? false : policy.equals(other);
        }

        public String toString() {
            return this.policy;
        }
    }

    private static FieldBuilderCache builderCache = new FieldBuilderCache();

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

    private AliasBuilder aliasBuilder = new AliasBuilder(); // TOCONSIDER: move to DB?
    private BuilderInfo bi;
    private List<ConstraintBuilder> constraints;
    private ThrowingFunction<Map.Entry<Persistable, List<Persistable>>, SqlBuilder, Exception> entryKeyToSqlBuilder = (entry) -> SqlBuilder.of(entry.getKey());
    private JoinPolicy joinPolicy = JoinPolicy.LEFT_JOIN;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ThrowingFunction<Persistable, SqlBuilder, Exception> persistableToSqlBuilder = (persistable) -> SqlBuilder.of(persistable);
    private Persistable refType;

    public <T extends Persistable> SqlBuilder(T refType) throws DBValidityException {

        bi = builderCache.buildersFor(refType);
        this.refType = refType;
        this.constraints = refType.getConstraints();
    }

    public String buildCreateTable() {

        String fields = bi.fieldBuilders.stream()
                                        .map(fb -> fb.build())
                                        .collect(Collectors.joining(", "));

        String primaryKey = bi.fieldBuilders.stream()
                                            .filter(fb -> fb.isIdField())
                                            .map(fb -> fb.getName())
                                            .filter(s -> hasContent(s))
                                            .collect(Collectors.joining(", "));

        String foreignKey = bi.foreignKeyBuilders.stream()
                                                 .map(fb -> fb.buildForeignKey())
                                                 .filter(s -> hasContent(s))
                                                 .collect(Collectors.joining(","));

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
          .append(bi.tableName)
          .append(" (")
          .append(fields)
          .append(hasContent(primaryKey) ? ", PRIMARY KEY (" + primaryKey + ")" : "")
          .append(hasContent(foreignKey) ? ", " + foreignKey : "")
          .append(");");

        return sb.toString();
    }

    public String buildDelete() {

        String idsNamesValues = bi.fieldBuilders.stream()
                                                .filter(fb -> fb.isIdField())
                                                .map(fb -> fb.getName() + " = " + fb.getFieldValue(refType))
                                                .collect(Collectors.joining(", "));

        return new StringBuilder("DELETE FROM ").append(bi.tableName)
                                                .append(" WHERE ")
                                                .append(idsNamesValues)
                                                .append(";")
                                                .toString();
    }

    public String buildDropTable() {
        return new StringBuilder("DROP TABLE IF EXISTS ").append(bi.tableName)
                                                         .append(";")
                                                         .toString();
    }

    // TOIMPROVE: partial updates
    public String buildInsert() {

        String sqlColumns = bi.fieldBuilders.stream()
                                            .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                            .map(fieldBuilder -> fieldBuilder.getName())
                                            .collect(Collectors.joining(", "));

        String questionMarks = bi.fieldBuilders.stream()
                                               .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                               .map(fieldBuilder -> "?")
                                               .collect(Collectors.joining(", "));

        return new StringBuilder("INSERT INTO ").append(bi.tableName)
                                                .append(" (")
                                                .append(sqlColumns)
                                                .append(") VALUES(")
                                                .append(questionMarks)
                                                .append(");")
                                                .toString();
    }

    // TOCONSIDER: generalise even more: use couple of functions?
    public String buildSelectByFields() throws DBValidityException {

        if (constraints.size() == 0) {
            throw new DBValidityException("No constraints to build from: expecting at least one field constraint for table: " + bi.tableName);
        }

        if (bi.oneToManyBuilders.size() > 0 ||
            bi.oneToOneBuilders.size() > 0) {

            return buildSelectByFieldsCascading();

        } else {

            return buildSelectByFieldsSingular();
        }
    }

    // TOCONSIDER: generalise even more: use couple of functions?
    public String buildSelectByIds() throws DBValidityException {

        if (bi.oneToManyBuilders.size() > 0 ||
            bi.oneToOneBuilders.size() > 0) {

            return buildSelectByIdsCascading();

        } else {

            return buildSelectByIdsSingular();
        }
    }

    public String buildSelectByIdsLazy() throws DBValidityException {

        if (bi.oneToManyBuilders.size() > 0 ||
            bi.oneToOneBuilders.size() > 0) {

            return buildSelectByIdsWithoutOneToAny();

        } else {

            return buildSelectByIdsSingular();
        }
    }

    public String buildSelectByRefIds(SqlBuilder referenceBuilder) {

        SqlByFieldsParameters params = buildSqlByFieldsParametersSingularLazy();

        String refsByValues = buildRefsByValues(referenceBuilder);

        StringBuilder byFields;
        byFields = new StringBuilder("SELECT ").append(params.fieldNames)
                                               .append(" FROM ")
                                               .append(bi.tableName)
                                               .append(" AS ")
                                               .append(params.tableNameAlias)
                                               .append(" WHERE ")
                                               .append(hasContent(refsByValues) ? refsByValues : "")
                                               .append(hasContent(params.constraints) ? " AND " + params.constraints : "")
                                               .append(";");

        logger.debug("SQL by fields: " + byFields);
        return byFields.toString();
    }

    public String buildUpdate() {

        String fields = bi.fieldBuilders.stream()
                                        .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                        .map(fieldBuilder -> fieldBuilder.getName() + " = ?")
                                        .collect(Collectors.joining(", "));

        return format("UPDATE %s SET %s;", bi.tableName, fields);
    }

    public AliasBuilder getAliasBuilder() {
        return aliasBuilder;
    }

    public List<ConstraintBuilder> getConstraints() {
        return constraints;
    }

    public List<FieldBuilder> getFieldBuilders() {
        return bi.fieldBuilders;
    }

    public List<FieldBuilder> getForeignKeyBuilders() {
        return bi.foreignKeyBuilders;
    }

    public List<FieldBuilder> getIdBuilders() {
        return bi.idBuilders;
    }

    public List<FieldBuilder> getOneToManyBuilders() {
        return bi.oneToManyBuilders;
    }

    public List<FieldBuilder> getOneToOneBuilders() {
        return bi.oneToOneBuilders;
    }

    public FieldBuilder getPrimaryKeyBuilder() {
        return bi.primaryKeyBuilder;
    }

    public Stream<Persistable> getReferenceCollectionPersistables(Persistable rootRef) throws DBValidityException {

        SqlBuilder rootBuilder = SqlBuilder.of(rootRef);
        return rootBuilder.getOneToManyBuilders().stream()
                          .map(collectionBuilder -> collectionBuilder.getFieldValue(rootRef))
                          .filter(object -> object != null && object instanceof Collection<?>)
                          .flatMap(object -> flattenToStream((Collection<?>) object))
                          .filter(object -> object instanceof Persistable)
                          .map(object -> (Persistable) object);
    }

    public Stream<Persistable> getReferencePersistables(Persistable rootRef) throws DBValidityException {

        SqlBuilder rootBuilder = SqlBuilder.of(rootRef);
        return rootBuilder.getOneToOneBuilders()
                          .stream()
                          .map(oneToOneBuilder -> (Persistable) oneToOneBuilder.getFieldValue(rootRef))
                          .filter(persistable -> persistable != null);
    }

    public Map<Persistable, List<Persistable>> getReferencePersistablesByRootCascading(Persistable rootRef) throws DBValidityException {

        Map<Persistable, List<Persistable>> refsByRoot = new LinkedHashMap<>();

        List<Persistable> refPersistables = Stream.concat(getReferenceCollectionPersistables(rootRef),
                                                          getReferencePersistables(rootRef))
                                                  .filter(p -> p != null)
                                                  .collect(Collectors.toMap(Persistable::getClass, p -> p, (p, q) -> p))
                                                  .values()
                                                  .stream()
                                                  .collect(Collectors.toList());

        refsByRoot.put(rootRef, refPersistables);

        refPersistables.stream()
                       .map((ThrowingFunction<Persistable, Map<Persistable, List<Persistable>>, Exception>) refPersistable -> {

                           return getReferencePersistablesByRootCascading(refPersistable);
                       })
                       .forEachOrdered(refsByRoot::putAll);

        return refsByRoot;
    }

    // TOCONSIDER: change to SqlBuilder left, SqlBuilder right, get refs, swap them and get refs again, return as list.
    public Stream<FieldReference> getReferences(SqlBuilder referredBuilder, SqlBuilder referringBuilder) {

        List<FieldBuilder> referring = referringBuilder.getForeignKeyBuilders();
        List<FieldBuilder> referred = referredBuilder.getFieldBuilders();

        return referring.stream()
                        .filter(fk -> fk instanceof ForeignKeyBuilder)
                        .map(fk -> (ForeignKeyBuilder) fk)
                        .flatMap(fk -> {
                            return referred.stream()
                                           .filter(fb -> referredBuilder.getTableName().equals(fk.getReferences()) &&
                                                         fb.getName().equals(fk.getField()))
                                           .map(fb -> new FieldReference(referredBuilder.getType(),
                                                                         referredBuilder.getTableName(),
                                                                         fb,
                                                                         referringBuilder.getType(),
                                                                         referringBuilder.getTableName(),
                                                                         fk));
                        });
    }

    public String getTableName() {
        return bi.tableName;
    }

    public Persistable getType() {
        return refType;
    }

    public Class<?> getTypeClass() {
        return refType.getClass();
    }

    @Override
    public String toString() {

        return String.format("SqlBuilder('%s')", refType.getClass().getSimpleName());
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
                          .map(constraintBuilder -> tableNameAlias + "." + constraintBuilder.build())
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

    private String buildFieldNamesWithoutOneToAny(List<FieldBuilder> fieldBuilders, String tableNameAlias) {
        return fieldBuilders.stream()
                            .filter(fb -> !fb.isOneToManyField() && !fb.isOneToOneField())
                            .map(fb -> tableNameAlias + "." + fb.getName())
                            .collect(Collectors.joining(", "));
    }

    private String buildJoins(List<FieldReference> references) {

        return references.stream()
                         .map(ref -> {
                             StringBuilder sb = new StringBuilder(joinPolicy.toString());
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

    private String buildRefsByValues(SqlBuilder referenceBuilder) {
        return this.getReferences(this, referenceBuilder)
                   .map((ThrowingFunction<FieldReference, String, Exception>) fieldReference -> {

                       return aliasBuilder.aliasOf(fieldReference.referringTable) +
                              fieldReference.referringField.getName() +
                              fieldReference.referredField.getFieldValue(refType).toString();
                   })
                   .collect(Collectors.joining(" AND "));
    }

    private String buildRootRefIds(SqlByFieldsParameters params) {

        return bi.idBuilders.stream()
                            .map(fb -> params.tableNameAlias + "." + fb.getName() + " = " + fb.getFieldValue(refType))
                            .collect(Collectors.joining(", "));
    }

    private String buildSelectByFieldsCascading() throws DBValidityException {

        SqlByFieldsParameters params = buildSqlByFieldsParametersCascading();

        StringBuilder byFields = new StringBuilder("SELECT ");

        byFields.append(params.fieldNames)
                .append(" FROM ")
                .append(bi.tableName)
                .append(" AS ")
                .append(params.tableNameAlias)
                .append(hasContent(params.joins) ? " " + params.joins : "")
                .append(" WHERE ")
                .append(hasContent(params.constraints) ? params.constraints : "")
                .append(";");

        logger.debug("SQL by fields: " + byFields);
        return byFields.toString();
    }

    private String buildSelectByFieldsSingular() throws DBValidityException {

        SqlByFieldsParameters params = buildSqlByFieldsParametersSingular();

        StringBuilder byFields = new StringBuilder("SELECT ").append(params.fieldNames)
                                                             .append(" FROM ")
                                                             .append(bi.tableName)
                                                             .append(" AS ")
                                                             .append(params.tableNameAlias)
                                                             .append(" WHERE ")
                                                             .append(params.constraints)
                                                             .append(";");

        logger.debug("SQL by fields: " + byFields);
        return byFields.toString();
    }

    private String buildSelectByIdsCascading() throws DBValidityException {

        StringBuilder byFields = new StringBuilder("SELECT ");

        SqlByFieldsParameters params = buildSqlByFieldsParametersCascading();

        String rootRefIds = buildRootRefIds(params);

        byFields.append(params.fieldNames)
                .append(" FROM ")
                .append(bi.tableName)
                .append(" AS ")
                .append(params.tableNameAlias)
                .append(hasContent(params.joins) ? " " + params.joins : "")
                .append(" WHERE ")
                .append(rootRefIds)
                .append(hasContent(params.constraints) ? " AND " + params.constraints : "")
                .append(";");

        logger.debug("SQL by ids: " + byFields);
        return byFields.toString();
    }

    private String buildSelectByIdsSingular() {

        SqlByFieldsParameters params = buildSqlByFieldsParametersSingular();

        StringBuilder byFields = buildSelectByIdsSingular(params);

        logger.debug("SQL by fields: " + byFields);
        return byFields.toString();
    }

    private StringBuilder buildSelectByIdsSingular(SqlByFieldsParameters params) {

        String rootRefIds = buildRootRefIds(params);

        StringBuilder byFields = new StringBuilder("SELECT ").append(params.fieldNames)
                                                             .append(" FROM ")
                                                             .append(bi.tableName)
                                                             .append(" AS ")
                                                             .append(params.tableNameAlias)
                                                             .append(" WHERE ")
                                                             .append(rootRefIds)
                                                             .append(hasContent(params.constraints) ? " AND " + params.constraints : "")
                                                             .append(";");
        return byFields;
    }

    private String buildSelectByIdsWithoutOneToAny() {

        SqlByFieldsParameters params = buildSqlByFieldsParametersSingularLazy();

        StringBuilder byFields = buildSelectByIdsSingular(params);

        logger.debug("SQL by ids: " + byFields);
        return byFields.toString();
    }

    private SqlByFieldsParameters buildSqlByFieldsParametersCascading() throws DBValidityException {

        Map<Persistable, List<Persistable>> referencesByRoot = getReferencePersistablesByRootCascading(refType);

        Map<SqlBuilder, List<SqlBuilder>> sqlBuildersByRoot = getSqlBuildersByRoot(referencesByRoot);

        List<FieldReference> fieldReferencesByRoot = getFieldReferences(sqlBuildersByRoot);

        List<SqlBuilder> uniqueBuilders = getUniqueBuilders(sqlBuildersByRoot);

        String fieldNames = buildFieldNames(uniqueBuilders);

        String joins = buildJoins(fieldReferencesByRoot);

        String constraintsString = buildConstraints(uniqueBuilders);

        String tableNameAlias = aliasBuilder.aliasOf(bi.tableName);

        return new SqlByFieldsParameters(fieldNames, joins, constraintsString, tableNameAlias);
    }

    private SqlByFieldsParameters buildSqlByFieldsParametersSingular() {

        String tableNameAlias = aliasBuilder.aliasOf(bi.tableName);

        String fieldNames = buildFieldNames(bi.fieldBuilders, tableNameAlias);

        String constraintsString = buildConstraints(tableNameAlias, constraints);

        return new SqlByFieldsParameters(fieldNames, "", constraintsString, tableNameAlias);
    }

    private SqlByFieldsParameters buildSqlByFieldsParametersSingularLazy() {

        String tableNameAlias = aliasBuilder.aliasOf(bi.tableName);

        String fieldNames = buildFieldNamesWithoutOneToAny(bi.fieldBuilders, tableNameAlias);

        String constraintsString = buildConstraints(tableNameAlias, constraints);

        return new SqlByFieldsParameters(fieldNames, "", constraintsString, tableNameAlias);
    }

    private List<FieldReference> getFieldReferences(Map<SqlBuilder, List<SqlBuilder>> sqlBuildersByRoot) {

        return sqlBuildersByRoot.entrySet()
                                .stream()
                                .flatMap(entry -> {
                                    SqlBuilder rootBuilder = entry.getKey();
                                    List<SqlBuilder> refBuilders = entry.getValue();
                                    return refBuilders.stream()
                                                      .flatMap(refBuilder -> getReferences(rootBuilder, refBuilder));

                                })
                                .collect(Collectors.toList());
    }

    private Map<SqlBuilder, List<SqlBuilder>> getSqlBuildersByRoot(Map<Persistable, List<Persistable>> referencesByRoot) {
        // TOCONSIDER: replace the method that creates the referencesByRoot with one that creates buildersByRoot, to remove this.
        return referencesByRoot.entrySet()
                               .stream()
                               .collect(Collectors.toMap(entryKeyToSqlBuilder,
                                                         (entry) -> {
                                                             return entry.getValue()
                                                                         .stream()
                                                                         .map(persistableToSqlBuilder)
                                                                         .collect(Collectors.toList());
                                                         } ,
                                                         (p, q) -> p,
                                                         () -> new LinkedHashMap<SqlBuilder, List<SqlBuilder>>()));
    }

    private List<SqlBuilder> getUniqueBuilders(Map<SqlBuilder, List<SqlBuilder>> sqlBuildersByRoot) {

        List<SqlBuilder> builders;
        builders = sqlBuildersByRoot.entrySet().stream()
                                    .flatMap(entry -> {

                                        SqlBuilder rootBuilder = entry.getKey();
                                        List<SqlBuilder> refBuilders = entry.getValue();

                                        return Stream.concat(Stream.of(rootBuilder), refBuilders.stream());

                                    })
                                    .collect(Collectors.toList());

        List<SqlBuilder> uniqueSortedBuilders;
        uniqueSortedBuilders = builders.stream()
                                       .collect(Collectors.toMap(SqlBuilder::getTypeClass, p -> p, (p, q) -> p))
                                       .values()
                                       .stream()
                                       .sorted((p1, p2) -> p1.getTypeClass()
                                                             .getSimpleName().compareTo(p2.getTypeClass()
                                                                                          .getSimpleName()))
                                       .collect(Collectors.toList());
        return uniqueSortedBuilders;
    }
}
