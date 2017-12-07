package mg.restgen.service;

public class ServiceException extends Exception {

    private static final long serialVersionUID = 06122017270777L;
    public final ServiceResult serviceResult;


    public ServiceException(String message, ServiceResult serviceResult) {
        this.serviceResult = serviceResult;
    }
}
