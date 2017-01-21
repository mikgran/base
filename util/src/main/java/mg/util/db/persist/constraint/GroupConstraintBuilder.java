package mg.util.db.persist.constraint;

import java.util.List;

public class GroupConstraintBuilder extends ConstraintBuilder {

    private final List<ConstraintBuilder> constraints;

    public GroupConstraintBuilder(List<ConstraintBuilder> constraints) {
        super("[multipleFields]"); // should never appear anywhere.
        this.constraints = constraints;
    }

    @Override
    public String build() {
        return null;
    }

    public List<ConstraintBuilder> getConstraints() {
        return constraints;
    }

}
