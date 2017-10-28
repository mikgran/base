package mg.restgen.service;

import java.util.List;

class ServiceParameters {
    public final List<RestService> services;
    public final Class<? extends Object> classRef;
    public final String nameRef;

    public ServiceParameters(List<RestService> services, Class<? extends Object> classRef, String nameRef) {
        super();
        this.services = services;
        this.classRef = classRef;
        this.nameRef = nameRef;
    }

}