package mg.util;

public class NotYetImplementedException extends RuntimeException {

    private static final long serialVersionUID = 270777211215L;

    public NotYetImplementedException() {
        super();
    }

    public NotYetImplementedException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Not Yet Implemented.";
    }
}
