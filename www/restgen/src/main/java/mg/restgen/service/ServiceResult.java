package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

public class ServiceResult {

    public static final ServiceResult resultOk = new ServiceResult(200, "");
    public static final ServiceResult noContent = new ServiceResult(204, "");
    public static final ServiceResult resultBadQuery = new ServiceResult(400, "");
    public static final ServiceResult resultInternalError = new ServiceResult(500, "");

    public final int statusCode;
    public final String message;
    public final String payload;

    public static ServiceResult badQuery() {
        return resultBadQuery;
    }

    public static ServiceResult badQuery(String message) {
        validateNotNull("message", message);
        return new ServiceResult(400, message);
    }

    public static ServiceResult internalError() {
        return resultInternalError;
    }

    public static ServiceResult internalError(String message) {
        validateNotNull("message", message);
        return new ServiceResult(500, message);
    }

    public static ServiceResult noContent() {
        return noContent;
    }

    public static ServiceResult ok() {
        return resultOk;
    }

    public static ServiceResult ok(String payload) {
        return ok("", payload);
    }

    public static ServiceResult ok(String message, String payload) {
        return new ServiceResult(200, message, payload);
    }

    public ServiceResult(int statusCode, String message) {
        this(statusCode, message, "");
    }

    public ServiceResult(int statusCode, String message, String payload) {
        this.statusCode = statusCode;
        this.message = message;
        this.payload = payload;
    }
}
