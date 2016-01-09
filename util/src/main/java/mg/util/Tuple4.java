package mg.util;

//a super naive tuple without any hash or equals
public class Tuple4<A, B, C, D> {

    public final A _1;
    public final B _2;
    public final C _3;
    public final D _4;

    public Tuple4(A _1, B _2, C _3, D _4) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
    }

    @Override
    public String toString() {

        return new StringBuilder("(").append((_1 != null ? _1 : "[null]"))
                                     .append(", ")
                                     .append((_2 != null ? _2 : "[null]"))
                                     .append(", ")
                                     .append((_3 != null ? _3 : "[null]"))
                                     .append(", ")
                                     .append((_4 != null ? _4 : "[null]"))
                                     .append(")")
                                     .toString();
    }
}
