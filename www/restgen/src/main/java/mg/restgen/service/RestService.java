package mg.restgen.service;

import java.util.List;

public interface RestService {

    public void apply(Object object);

    public List<Class<?>> getAcceptableTypes();

    public List<String> getActions();
}