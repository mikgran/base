package mg.restgen.service;

import java.util.List;

class ServiceInfo {
    public final List<RestService> services;
    public final Class<? extends Object> classRef;
    public final String nameRef;
    public final String command;

    public ServiceInfo(List<RestService> services,
                       Class<? extends Object> classRef,
                       String nameRef,
                       String command) {
        super();
        this.services = services;
        this.classRef = classRef;
        this.nameRef = nameRef;
        this.command = command;
    }

}