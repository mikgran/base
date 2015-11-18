package mg.util.db.persist;

public class DbValidityException extends Exception {

    private static final long serialVersionUID = 1L;

    public DbValidityException(String message) {
        super(message);
    }
}
