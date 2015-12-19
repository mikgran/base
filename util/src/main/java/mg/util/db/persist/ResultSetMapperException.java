package mg.util.db.persist;

public class ResultSetMapperException extends Exception {

    private static final long serialVersionUID = 27707707122015L;
    
    public ResultSetMapperException(String message) {
        super(message);
    }

    public ResultSetMapperException(Throwable cause) {
        super(cause);
    }
}
