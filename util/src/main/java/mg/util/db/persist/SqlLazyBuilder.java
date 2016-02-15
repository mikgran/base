package mg.util.db.persist;

public class SqlLazyBuilder extends SqlBuilder {

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


}
