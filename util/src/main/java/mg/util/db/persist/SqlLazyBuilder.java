package mg.util.db.persist;

import static mg.util.Common.hasContent;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mg.util.db.persist.field.FieldBuilder;
import mg.util.functional.function.ThrowingFunction;

public class SqlLazyBuilder extends SqlBuilder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public <T extends Persistable> SqlLazyBuilder(T refType) throws DBValidityException {
        super(refType);
    }

    public String buildSelectByIds() throws DBValidityException {

        if (hasRefTypeOneToAnyReferences()) {

            return buildSelectByIdsWithoutOneToAny();
        } else {
            return buildSelectByIdsSingular();
        }
    }

    // published for testing purposes
    protected String buildSelectByRefIds(SqlBuilder referenceBuilder) {

        SqlByFieldsParameters params = buildSqlByFieldsParametersSingularLazy(referenceBuilder);
        String refsByValues = buildRefsByValues(referenceBuilder);
        String referredTableName = referenceBuilder.getBuilderInfo().tableName;

        StringBuilder byFields;
        byFields = new StringBuilder("SELECT ").append(params.fieldNames)
                                               .append(" FROM ")
                                               .append(referredTableName)
                                               .append(" AS ")
                                               .append(params.tableNameAlias)
                                               .append(" WHERE ")
                                               .append(hasContent(refsByValues) ? refsByValues : "")
                                               .append(hasContent(params.constraints) ? " AND " + params.constraints : "")
                                               .append(";");

        logger.debug("SQL by fields: " + byFields);
        return byFields.toString();
    }

    private String buildFieldNamesWithoutOneToAny(List<FieldBuilder> fieldBuilders, String tableNameAlias) {
        return fieldBuilders.stream()
                            .filter(fb -> !fb.isOneToManyField() && !fb.isOneToOneField())
                            .map(fb -> tableNameAlias + "." + fb.getName())
                            .collect(Collectors.joining(", "));
    }

    private String buildRefsByValues(SqlBuilder referenceBuilder) {
        return this.getReferences(this, referenceBuilder)
                   .map((ThrowingFunction<FieldReference, String, Exception>) fieldReference -> {

                       String retVal = aliasBuilder.aliasOf(fieldReference.referringTable) +
                                       "." +
                                       fieldReference.referringField.getName() +
                                       " = " +
                                       fieldReference.referredField.getFieldValue(refType).toString();

                       return retVal;
                   })
                   .collect(Collectors.joining(" AND "));
    }

    private String buildSelectByIdsWithoutOneToAny() {

        SqlByFieldsParameters params = buildSqlByFieldsParametersSingularLazy(this);
        StringBuilder byFields = buildSelectByIdsSingular(params);

        logger.debug("SQL by ids: " + byFields);
        return byFields.toString();
    }

    private SqlByFieldsParameters buildSqlByFieldsParametersSingularLazy(SqlBuilder referenceBuilder) {

        BuilderInfo bi = referenceBuilder.getBuilderInfo();
        String tableNameAlias = aliasBuilder.aliasOf(bi.tableName);
        String fieldNames = buildFieldNamesWithoutOneToAny(bi.fieldBuilders, tableNameAlias);
        String constraintsString = buildConstraints(tableNameAlias, referenceBuilder.getConstraints());

        return new SqlByFieldsParameters(fieldNames, "", constraintsString, tableNameAlias);
    }
}
