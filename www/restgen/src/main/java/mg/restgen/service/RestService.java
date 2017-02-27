package mg.restgen.service;

import java.util.List;

public interface RestService {

    // TOCONDSIDER: apply actions to objects or apply sets of actions to multiple objects
    // - case simple: save Contact
    // - case complex: save Contact, book a room and mail a confirmation, action save-contact-book-room-mail-confirmation
    //                  or: action1: save contact, action2: boom a room, action3: mail a confirmation
    public void apply(Object object, Action action);

    public List<Class<?>> getAcceptableTypes();

    /**
     * Returns the callable actions of this service.
     *
     * @return
     */
    public List<Action> getActions();
}