package mg.util.db.persist;

public class DBValidityException extends Exception {

    private static final long serialVersionUID = 1L;

    public DBValidityException(String message) {
        super(message);
    }
}
