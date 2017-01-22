package mg.util.db.persist.constraint;

import java.util.List;
import java.util.stream.Collectors;

public class GroupConstraintBuilder extends ConstraintBuilder {

    private final List<ConstraintBuilder> constraints;

    public GroupConstraintBuilder(List<ConstraintBuilder> constraints) {
        super("[multipleFields]"); // should never appear anywhere.
        this.constraints = constraints;
        System.out.println("constraints.toString(): " + constraints.toString());
    }

    @Override
    public String build() {

        return constraints.stream()
                          .map(ConstraintBuilder::build)
                          .collect(Collectors.joining(" "));
    }

    public List<ConstraintBuilder> getConstraints() {
        return constraints;
    }

}
