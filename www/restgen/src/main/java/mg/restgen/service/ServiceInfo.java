package mg.restgen.service;

import java.util.List;

class ServiceInfo {
    public final List<RestService> services;
    public final Class<? extends Object> classRef;
    public final String nameRef;

    public ServiceInfo(List<RestService> services, Class<? extends Object> classRef, String nameRef) {
        super();
        this.services = services;
        this.classRef = classRef;
        this.nameRef = nameRef;
    }

}