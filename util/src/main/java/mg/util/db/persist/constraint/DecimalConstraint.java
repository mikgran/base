package mg.util.db.persist.constraint;

import mg.util.NotYetImplementedException;

public class DecimalConstraint extends Constraint {
    public DecimalConstraint(String fieldName, int i) {
        super(fieldName);
    }

    @Override
    public String get() {
        throw new NotYetImplementedException();
    }
}
