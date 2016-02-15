package mg.util.db.persist;

import static mg.util.Common.hasContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private String buildSelectByIdsWithoutOneToAny() {

        SqlByFieldsParameters params = buildSqlByFieldsParametersSingularLazy(this);
        StringBuilder byFields = buildSelectByIdsSingular(params);

        logger.debug("SQL by ids: " + byFields);
        return byFields.toString();
    }
}
