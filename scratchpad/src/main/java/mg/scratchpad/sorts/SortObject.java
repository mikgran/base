package mg.scratchpad.sorts;

import java.util.Objects;

public class SortObject implements Comparable<SortObject> {

    protected String a = "";
    protected String b = "";
    protected int c = 0;

    public SortObject() {
        super();
    }

    public SortObject(String a, String b, int c) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        SortObject other = (SortObject) obj;
        
        return Objects.equals(this.a, other.a) &&
            Objects.equals(this.b, other.b) &&
            Objects.equals(this.c, other.c);
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.a, this.b, this.c);
    }
    
    @Override
    public int compareTo(SortObject o) {

        // compare on the basis of a then b, then c
        if (this.a != o.a) {
            return this.a.compareTo(o.a);
        }

        if (this.b != o.b) {
            return this.b.compareTo(o.b);
        }

        if (this.c != o.c) {
            return (this.c -
                o.c); // ascending order
        }

        // the objects were equal
        return 0;
    }
    
    @Override
    public String toString() {
        return String.format("[a: %s, b: %s, c: %d]", a, b, c);
    }

}
