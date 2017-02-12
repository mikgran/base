package mg.restgen.service;

public interface RestService {

    public void apply(Object object);

    public Class<?> getAcceptableTypes();
}