package mg.util;

// a super naive tuple without any hash or equals
public class Tuple2<A, B> {

    public final A _1;
    public final B _2;

    public static <A, B> Tuple2<A, B> of(A _1, B _2) {
        return new Tuple2<>(_1, _2);
    }

    public Tuple2(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }

    @Override
    public String toString() {

        return new StringBuilder("(").append((_1 != null ? _1 : "[null]"))
                                     .append(", ")
                                     .append((_2 != null ? _2 : "[null]"))
                                     .append(")")
                                     .toString();
    }
}
