package mg.restgen.service;

import mg.util.ToStringBuilder;

public class ServiceResult {

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int BAD_QUERY = 400;
    public static final int INTERNAL_ERROR = 500;

    public static final ServiceResult resultOk = new ServiceResult(OK, "");
    public static final ServiceResult resultCreated = new ServiceResult(CREATED, "");
    public static final ServiceResult resultNoContent = new ServiceResult(NO_CONTENT, "");
    public static final ServiceResult resultBadQuery = new ServiceResult(BAD_QUERY, "");
    public static final ServiceResult resultInternalError = new ServiceResult(INTERNAL_ERROR, "");

    public final int statusCode;
    public final String message;
    public final Object payload;

    public static ServiceResult badQuery() {
        return resultBadQuery;
    }

    public static ServiceResult badQuery(String message) {
        return new ServiceResult(BAD_QUERY, message);
    }

    public static ServiceResult created() {
        return resultCreated;
    }

    public static ServiceResult internalError() {
        return resultInternalError;
    }

    public static ServiceResult internalError(String message) {
        return new ServiceResult(INTERNAL_ERROR, message);
    }

    public static ServiceResult noContent() {
        return resultNoContent;
    }

    public static ServiceResult noContent(String message) {
        return new ServiceResult(NO_CONTENT, message);
    }

    public static ServiceResult ok() {
        return resultOk;
    }

    public static ServiceResult ok(Object payload) {
        return ok("", payload);
    }

    public static ServiceResult ok(String message, Object payload) {
        return new ServiceResult(OK, message, payload);
    }

    public ServiceResult(int statusCode, String message) {
        this(statusCode, message, "");
    }

    public ServiceResult(int statusCode, String message, Object payload) {
        this.statusCode = statusCode;
        this.message = message;
        this.payload = payload;
    }

    @Override
    public String toString() {

        return ToStringBuilder.of(this)
                              .add(t -> "" + t.statusCode)
                              .add(t -> "" + t.message)
                              .add(t -> "" + t.payload)
                              .build();
    }
}
