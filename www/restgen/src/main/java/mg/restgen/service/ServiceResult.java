package mg.restgen.service;

public class ServiceResult {

    public static final ServiceResult resultOk = new ServiceResult(200, "");

    public final int statusCode;
    public final String message;

    public static ServiceResult ok() {
        return resultOk;
    }

    public ServiceResult(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
