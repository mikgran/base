package mg.util.db.persist;

public class DBMappingException extends Exception {

    private static final long serialVersionUID = 27707707122015L;

    public DBMappingException(String message) {
        super(message);
    }

    public DBMappingException(Throwable cause) {
        super(cause);
    }
}
