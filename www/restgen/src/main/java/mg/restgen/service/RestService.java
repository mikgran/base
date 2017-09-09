package mg.restgen.service;

import static mg.util.validation.Validator.validateNotNull;

import java.util.List;

public interface RestService {

    /*
     * Applies the service actions to an object:
     * - service should be very concise - or as minimal as possible
     * - break bigger services into multiple smaller services for re-usability
     *      -
     *
     *     */
    // TOCONDSIDER: apply service actions to objects or apply sets of actions to multiple objects
    // - case simple: save Contact
    // - case complex: save Contact, book a room and mail a confirmation, action save-contact-book-room-mail-confirmation
    //                  or: action1: save contact, action2: book a room, action3: mail a confirmation
    public void apply(Object object);

    public List<Class<?>> getAcceptableTypes();

    /**
     * Accepts type by comparing object to the acceptable types list.
     * returns Returns true if acceptable, false otherwise.
     * throws Throws IllegalArgumentEexception for null/invalid acceptableTypes.
     */
    default public boolean isAcceptable(Object o) {

        validateNotNull("o", o);

        List<Class<?>> acceptableTypes = getAcceptableTypes();

        validateNotNull("acceptableTypes", acceptableTypes);

        return acceptableTypes.stream()
                              .filter(a -> a.isAssignableFrom(o.getClass()))
                              .findFirst()
                              .isPresent();
    }

}
