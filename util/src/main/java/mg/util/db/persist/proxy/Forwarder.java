package mg.util.db.persist.proxy;

public interface Forwarder<T, S> {
    T to(S target);
}
