package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

public class ServiceResult {

    public static final ServiceResult resultOk = new ServiceResult(200, "");
    public static final ServiceResult resultBadQuery = new ServiceResult(400, "");

    public final int statusCode;
    public final String message;

    public static ServiceResult badQuery() {
        return resultBadQuery;
    }

    public static ServiceResult badQuery(String message) {
        validateNotNull("message", message);
        return new ServiceResult(400, message);
    }

    public static ServiceResult ok() {
        return resultOk;
    }

    public ServiceResult(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
