package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;
import static mg.util.validation.Validator.validateNotNullOrEmpty;

import java.util.function.Supplier;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import mg.util.ToStringBuilder;

/*
 * All the services are bound via two keys: the name reference of the service and the command.
 */
public class ServiceKey {

    public final String nameRef;
    public final String command;

    public static ServiceKey of(String nameRef, String command) {
        validateNotNullOrEmpty("nameRef", nameRef);
        validateNotNullOrEmpty("command", command);

        return new ServiceKey(nameRef.toLowerCase(), command);
    }

    public static ServiceKey of(String nameRef, String command, Supplier<ServiceException> nameRefOrCommandMissingExceptionSupplier) throws ServiceException {
        validateNotNull("exceptionProvider", nameRefOrCommandMissingExceptionSupplier);

        if (nameRef == null || command == null) {
            throw nameRefOrCommandMissingExceptionSupplier.get();
        }
        return of(nameRef, command);
    }

    public ServiceKey(String nameRef, String command) {
        this.nameRef = nameRef;
        this.command = command;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ServiceKey rhs = (ServiceKey) obj;
        return new EqualsBuilder().append(nameRef, rhs.nameRef) // evaluate only by fields.
                                  .append(command, rhs.command)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(171, 371).append(nameRef)
                                            .append(command)
                                            .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.of(this)
                              .add(t -> t.nameRef)
                              .add(t -> t.command)
                              .build();
    }

}
