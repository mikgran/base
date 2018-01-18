package mg.restgen.service;

import java.util.List;
import java.util.stream.Collectors;

import mg.util.ToStringBuilder;
import mg.util.validation.Validator;

public class ServiceInfo {
    public final List<RestService> generalServices;
    public final List<RestService> services;
    public final Class<?> classRef;
    public final String nameRef;
    public final String command;

    public static ServiceInfo of(List<RestService> services,
        Class<?> classRef,
        String nameRef,
        String command) {

        Validator.validateNotNull("services", services);
        Validator.validateNotNull("services", classRef);
        Validator.validateNotNull("services", nameRef);
        Validator.validateNotNull("services", command);

        return new ServiceInfo(services, classRef, nameRef, command);
    }

    private ServiceInfo(List<RestService> services,
        Class<?> classRef,
        String nameRef,
        String command) {
        super();
        this.services = services;
        this.classRef = classRef;
        this.nameRef = nameRef;
        this.command = command;
        this.generalServices = services.stream()
                                       .filter(s -> s.isGeneralService())
                                       .collect(Collectors.toList());
    }

    public void addToServices(RestService restService) {
        Validator.validateNotNull("", restService);

        if (!services.contains(restService)) {
            services.add(restService);
        }
        if (restService.isGeneralService() &&
            !generalServices.contains(restService)) {
            generalServices.add(restService);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.of(this)
                              .add(t -> "" + t.nameRef)
                              .add(t -> "" + t.command)
                              .add(t -> "" + t.generalServices)
                              .add(t -> "" + t.services.toString())
                              .build();
    }

}