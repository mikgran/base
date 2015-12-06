package mg.util.validation.rule;

import java.sql.Connection;

public class ConnectionNotClosed extends ValidationRule {

    private String message = "Connection must be open.";

    @Override
    public boolean apply(Object object) {

        if (!(object instanceof Connection)) {
            message = "Object must be an instance of Connection.";
            return false;
        }

        try {
            Connection connection = (Connection) object;
            if (!connection.isClosed()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    @Override
    public String getMessage() {

        return message;
    }

}
