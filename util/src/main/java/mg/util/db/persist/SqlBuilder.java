package mg.util.db.persist;

import static java.lang.String.format;
import static mg.util.Common.flattenToStream;
import static mg.util.Common.hasContent;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.field.FieldBuilder;
import mg.util.db.persist.field.ForeignKeyBuilder;
import mg.util.functional.function.ThrowingFunction;

class SqlBuilder {

    private static FieldBuilderCache builderCache = new FieldBuilderCache();

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

    private AliasBuilder aliasBuilder = new AliasBuilder(); // TOCONSIDER: move to DB?
    private BuilderInfo bi;
    private List<ConstraintBuilder> constraints;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Persistable refType;
    private ThrowingFunction<Persistable, SqlBuilder, Exception> toSqlBuilder = (persistable) -> SqlBuilder.of(persistable);

    public <T extends Persistable> SqlBuilder(T refType) throws DBValidityException {

        bi = builderCache.buildersFor(refType);
        this.refType = refType;
        constraints = refType.getConstraints();
    }

    public String buildCreateTable() {
        String fields = bi.getFieldBuilders().stream()
                          .map(fb -> fb.build())
                          .collect(Collectors.joining(", "));

        String primaryKey = bi.getFieldBuilders().stream()
                              .filter(fb -> fb.isIdField())
                              .map(fb -> fb.getName())
                              .filter(s -> hasContent(s))
                              .collect(Collectors.joining(", "));

        String foreignKey = bi.getForeignKeyBuilders().stream()
                              .map(fb -> fb.buildForeignKey())
                              .filter(s -> hasContent(s))
                              .collect(Collectors.joining(","));

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
          .append(bi.getTableName())
          .append(" (")
          .append(fields)
          .append(hasContent(primaryKey) ? ", PRIMARY KEY (" + primaryKey + ")" : "")
          .append(hasContent(foreignKey) ? ", " + foreignKey : "")
          .append(");");

        return sb.toString();
    }

    public String buildDelete() {
        String idsNamesValues = bi.getFieldBuilders().stream()
                                  .filter(fb -> fb.isIdField())
                                  .map(fb -> fb.getName() + " = " + fb.getFieldValue(refType))
                                  .collect(Collectors.joining(", "));

        return new StringBuilder("DELETE FROM ").append(bi.getTableName())
                                                .append(" WHERE ")
                                                .append(idsNamesValues)
                                                .append(";")
                                                .toString();
    }

    public String buildDropTable() {
        return new StringBuilder("DROP TABLE IF EXISTS ").append(bi.getTableName())
                                                         .append(";")
                                                         .toString();
    }

    // TOIMPROVE: partial updates
    public String buildInsert() {

        String sqlColumns = bi.getFieldBuilders().stream()
                              .filter(fieldBuilder -> !fieldBuilder.isIdField())
                              .map(fieldBuilder -> fieldBuilder.getName())
                              .collect(Collectors.joining(", "));

        String questionMarks = bi.getFieldBuilders().stream()
                                 .filter(fieldBuilder -> !fieldBuilder.isIdField())
                                 .map(fieldBuilder -> "?")
                                 .collect(Collectors.joining(", "));

        return new StringBuilder("INSERT INTO ").append(bi.getTableName())
                                                .append(" (")
                                                .append(sqlColumns)
                                                .append(") VALUES(")
                                                .append(questionMarks)
                                                .append(");")
                                                .toString();
    }

    // reference map: type.class -> [(refTableName.field, referringTableName.field), (refTableName.field, referringTableName.field)]
    public Map<Class<?>, List<FieldReference>> buildReferences(List<SqlBuilder> sqlBuilders) {

        Map<Class<?>, List<FieldReference>> refsByClass = new LinkedHashMap<>();

        // while loop since .stream().windowed(2) || .sliding(2) is missing, TOCONSIDER: write a windowed processor (spliterator? iterator?)
        if (sqlBuilders.size() > 1) {
            Iterator<SqlBuilder> sqlBuilderIterator = sqlBuilders.iterator();
            SqlBuilder left = null;
            SqlBuilder right = sqlBuilderIterator.next(); // sliding(2) || windowed(2)
            while (sqlBuilderIterator.hasNext()) {
                left = right;
                right = sqlBuilderIterator.next();

                List<FieldReference> fieldReferences = getReferences(left, right).collect(Collectors.toList());
                refsByClass.put(left.getType().getClass(), fieldReferences);
            }
        }

        return refsByClass;
    }

    public String buildSelectByFields() throws DBValidityException {

        if (constraints.size() == 0) {
            throw new DBValidityException("No constraints to build from: expecting at least one field constraint for table: " + bi.getTableName());
        }

        // TODO: buildSelectByFields: OneToMany, OneToOne relations
        if (bi.getOneToManyBuilders().size() > 0) {

            return buildSelectByFieldsCascading();
        } else {

            return buildSelectByFieldsSingular();
        }
    }

    public String buildSelectByIds() {

        String ids = bi.getIdBuilders().stream()
                       .map(fb -> fb.getName() + " = " + fb.getFieldValue(refType))
                       .collect(Collectors.joining(", "));

        String tableNameAlias = aliasBuilder.aliasOf(bi.getTableName());

        String fieldNames = buildFieldNames(bi.getFieldBuilders(), tableNameAlias);

        return new StringBuilder("SELECT ").append(fieldNames)
                                           .append(" FROM ")
                                           .append(bi.getTableName())
                                           .append(" AS ")
                                           .append(tableNameAlias)
                                           .append(" WHERE ")
                                           .append(ids)
                                           .append(";")
                                           .toString();
    }

    public String buildUpdate() {
        String fields = bi.getFieldBuilders().stream()
                          .filter(fieldBuilder -> !fieldBuilder.isIdField())
                          .map(fieldBuilder -> fieldBuilder.getName() + " = ?")
                          .collect(Collectors.joining(", "));

        return format("UPDATE %s SET %s;", bi.getTableName(), fields);
    }

    public AliasBuilder getAliasBuilder() {
        return aliasBuilder;
    }

    public List<ConstraintBuilder> getConstraints() {
        return constraints;
    }

    public List<FieldBuilder> getFieldBuilders() {
        return bi.getFieldBuilders();
    }

    public List<FieldBuilder> getForeignKeyBuilders() {
        return bi.getForeignKeyBuilders();
    }

    public List<FieldBuilder> getIdBuilders() {
        return bi.getIdBuilders();
    }

    public List<FieldBuilder> getOneToManyBuilders() {
        return bi.getOneToManyBuilders();
    }

    public List<FieldBuilder> getOneToOneBuilders() {
        return bi.getOneToOneBuilders();
    }

    public FieldBuilder getPrimaryKeyBuilder() {
        return bi.getPrimaryKeyBuilder();
    }

    public Map<Class<?>, List<SqlBuilder>> getReferenceBuildersByClassCascading(Persistable refType) throws DBValidityException {

        Map<Class<?>, List<Persistable>> refsByClass = new LinkedHashMap<>();

        Stream<Persistable> allRefs = Stream.concat(getReferenceCollectionPersistables(),
                                                    getReferencePersistables());

        Stream<Persistable> uniqueRefs = allRefs.collect(Collectors.toMap(Persistable::getClass,
                                                                          p -> p,
                                                                          (p, q) -> p))
                                                .values()
                                                .stream();

        refsByClass.put(refType.getClass(), uniqueRefs.collect(Collectors.toList()));

        return null;
    }

    // TOIMPROVE: clarity, brevity, meaning of naming
    // TOIMPROVE: add OneToOne, OneToMany
    public Stream<Persistable> getReferenceCollectionPersistables() throws DBValidityException {

        return bi.getOneToManyBuilders().stream()
                 .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getFieldValue(refType)))
                 .filter(object -> object instanceof Persistable)
                 .map(object -> (Persistable) object);
    }

    public Stream<Persistable> getReferencePeristablesCascading(Persistable persistable) throws DBValidityException {

        SqlBuilder sqlBuilder = SqlBuilder.of(persistable);

        Stream<Persistable> uniqueCollectionPersistables;
        uniqueCollectionPersistables = sqlBuilder.getOneToManyBuilders()
                                                 .stream()
                                                 .flatMap(collectionBuilder -> flattenToStream((Collection<?>) collectionBuilder.getFieldValue(sqlBuilder.getType())))
                                                 .filter(object -> object instanceof Persistable)
                                                 .map(object -> (Persistable) object)
                                                 .flatMap((ThrowingFunction<Persistable, Stream<Persistable>, Exception>) subPersistable -> {
                                                     return getReferencePeristablesCascading(subPersistable);
                                                 });

        return Stream.concat(Stream.of(persistable), uniqueCollectionPersistables);
    }

    public Stream<Persistable> getReferencePersistables() {
        return bi.getOneToOneBuilders()
                 .stream()
                 .map(oneToOneBuilder -> (Persistable) oneToOneBuilder.getFieldValue(refType))
                 .filter(p -> p != null);
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
        return bi.getTableName();
    }

    public Persistable getType() {
        return refType;
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

    private String buildJoins(Map<Class<?>, List<FieldReference>> references) {
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
        // TODO: buildSelectByFieldsCascading: build refs by Class<?>
        List<SqlBuilder> refBuilders = getReferenceBuilders(refType);

        getReferenceBuildersByClassCascading(refType);

        Map<Class<?>, List<FieldReference>> references = buildReferences(refBuilders);

        String joins = buildJoins(references);

        String constraints = buildConstraints(refBuilders);

        String tableNameAlias = aliasBuilder.aliasOf(bi.getTableName());

        String fieldNames = buildFieldNames(refBuilders);

        StringBuilder byFieldsSql = new StringBuilder("SELECT ");

        byFieldsSql.append(fieldNames)
                   .append(" FROM ")
                   .append(bi.getTableName())
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

        String tableNameAlias = aliasBuilder.aliasOf(bi.getTableName());

        String fieldNames = buildFieldNames(bi.getFieldBuilders(), tableNameAlias);

        String constraintsString = buildConstraints(tableNameAlias, constraints);

        StringBuilder byFields = new StringBuilder("SELECT ").append(fieldNames)
                                                             .append(" FROM ")
                                                             .append(bi.getTableName())
                                                             .append(" AS ")
                                                             .append(tableNameAlias)
                                                             .append(" WHERE ")
                                                             .append(constraintsString)
                                                             .append(";");

        logger.debug("SQL by fields: " + byFields);

        return byFields.toString();
    }

    private List<SqlBuilder> getReferenceBuilders(Persistable type) throws DBValidityException {

        return getReferencePeristablesCascading(type).map(toSqlBuilder)
                                                     .collect(Collectors.toList());
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
