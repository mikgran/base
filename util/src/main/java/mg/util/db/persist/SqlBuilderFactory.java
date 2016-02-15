package mg.util.db.persist;

public class SqlBuilderFactory {

    public static <T extends Persistable> SqlBuilder of(T t) throws DBValidityException {
        return new SqlBuilder(t);
    }

}
