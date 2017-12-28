package mg.restgen.service;

import java.util.List;

import mg.util.ToStringBuilder;

public class ServiceInfo {
    public final List<RestService> services;
    public final Class<? extends Object> classRef;
    public final String nameRef;
    public final String command;

    public static ServiceInfo of(List<RestService> services,
        Class<? extends Object> classRef,
        String nameRef,
        String command) {

        return new ServiceInfo(services, classRef, nameRef, command);
    }

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

    @Override
    public String toString() {
        return ToStringBuilder.of(this)
                              .add(t -> "" + t.nameRef)
                              .add(t -> "" + t.command)
                              .add(t -> "" + t.services.toString())
                              .build();
    }

}