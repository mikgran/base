package mg.restgen.service;

import java.util.List;

public interface RestService {

    public void apply(Object object);

    public List<Class<?>> getAcceptableTypes();

    /**
     * Returns the callable actions of this service.
     *
     * @return
     */
    public List<Action> getActions();
}