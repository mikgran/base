package mg.util;

import java.util.Objects;

public class Tuple<S, T> {

    private final S s;
    private final T t;

    /**
     * Constructs the Tuple. Accepts only non null objects:  
     * an IllegalArgumentException is thrown on any nulls.
     * @param s a nonNull type S object.
     * @param t a nonNull type T object.
     */
    public Tuple(S s, T t) {
        this.s = Objects.requireNonNull(s);
        this.t = Objects.requireNonNull(t);
    }

    public S getS() {
        return s;
    }

    public T getT() {
        return t;
    }
}
