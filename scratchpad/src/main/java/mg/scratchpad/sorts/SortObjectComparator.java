package mg.scratchpad.sorts;

import java.util.Comparator;

public class SortObjectComparator implements Comparator<SortObject> {

    public enum Field {
        A, B, C
    }

    private Field comparedField = Field.A;

    @SuppressWarnings("unused")
    private SortObjectComparator() { // force the use of constructor with
                                     // parameters.
        super();
    }

    public SortObjectComparator(Field comparedField) {
        super();

        this.comparedField = comparedField;
    }

    @Override
    public int compare(SortObject o1, SortObject o2) {

        int returnValue = 0;

        switch (this.comparedField) {
            case A :
                returnValue = o1.a.compareTo(o2.a);
                break;
            case B :
                returnValue = o1.b.compareTo(o2.b);
                break;
            case C :
                returnValue = (o1.c == o2.c) ? 0 : (o1.c < o2.c) ? -1 : 1;
                break;
        }

        return returnValue;
    }

}
