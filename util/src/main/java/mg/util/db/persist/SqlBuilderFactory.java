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
     * @param db contains FetchPolicy which is used to create the SqlBuilder.
     * @return a new SqlBuilder: FetchPolicy.LAZY creates a SqlLazyBuilder, EAGER creates a SqlBuilder.
     */
    public static <T extends Persistable> SqlBuilder of(T t, DB db) throws DBValidityException {

        if (FetchPolicy.EAGER.equals(db.getFetchPolicy())) {

            return new SqlBuilder(t);
        } else {
            return new SqlLazyBuilder(t);
        }

    }

}
