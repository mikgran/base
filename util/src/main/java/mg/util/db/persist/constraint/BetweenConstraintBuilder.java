package mg.util.db.persist.constraint;

import java.time.LocalDateTime;

public class BetweenConstraintBuilder extends ConstraintBuilder {

    private LocalDateTime lowerConstraint;
    private LocalDateTime upperConstraint;

    public BetweenConstraintBuilder(String fieldName, LocalDateTime lowerConstraint, LocalDateTime upperConstraint) {
        super(fieldName);
        this.lowerConstraint = lowerConstraint;
        this.upperConstraint = upperConstraint;
    }

    @Override
    public String build() {
        return new StringBuilder().append(fieldName)
                                  .append(" BETWEEN '")
                                  .append(lowerConstraint)
                                  .append("' AND '")
                                  .append(upperConstraint)
                                  .append("'")
                                  .toString();
    }

}
