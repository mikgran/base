package mg.util.db.persist;

public class SqlBuilderFactory {

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

    public static <T extends Persistable> SqlBuilder of(T t, FetchPolicy fetchPolicy) throws DBValidityException {

        if (FetchPolicy.EAGER.equals(fetchPolicy)) {

            return new SqlBuilder(t);
        } else {
            return new SqlLazyBuilder(t);
        }

    }

}
