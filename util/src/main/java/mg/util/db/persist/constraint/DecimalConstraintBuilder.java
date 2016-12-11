package mg.util.db.persist.constraint;

import mg.util.NotYetImplementedException;

public class DecimalConstraintBuilder extends ConstraintBuilder {

    public DecimalConstraintBuilder(String fieldName, int i) {
        super(fieldName);
    }

    public DecimalConstraintBuilder(String fieldName, long i) {
        super(fieldName);
    }

    @Override
    public String build() {
        throw new NotYetImplementedException();
    }
}
