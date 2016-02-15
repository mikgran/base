package mg.util.db.persist;

public class SqlBuilderFactory {

    /**
     * Creates a SqlBuilder with FetchPolicy.EAGER.
     * @param t the object to create the builder for.
     * @return a new SqlBuilder.
     */
    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

    /**
     * Creates a SqlBuilder with specified FetchPolicy.
     * @param t the object to create the builder for.
     * @param fetchPolicy
     * @return a new SqlBuilder: FetchPolicy.LAZY creates a SqlLazyBuilder, EAGER creates a SqlBuilder.
     */
    public static <T extends Persistable> SqlBuilder of(T t, FetchPolicy fetchPolicy) throws DBValidityException {

        if (FetchPolicy.EAGER.equals(fetchPolicy)) {

            return new SqlBuilder(t);
        } else {
            return new SqlLazyBuilder(t);
        }

    }

}
