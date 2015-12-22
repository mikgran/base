package mg.util;

public class NotYetImplementedException extends RuntimeException {

    private static final long serialVersionUID = 270777211215L;

    @Override
    public String getMessage() {
        return "Not Yet Implemented.";
    }
}
