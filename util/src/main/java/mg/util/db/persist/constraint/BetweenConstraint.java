package mg.util.db.persist.constraint;

import java.time.LocalDateTime;

public class BetweenConstraint extends Constraint {

    private LocalDateTime lowerConstraint;
    private LocalDateTime upperConstraint;

    public BetweenConstraint(String fieldName, LocalDateTime lowerConstraint, LocalDateTime upperConstraint) {
        super(fieldName);
        this.lowerConstraint = lowerConstraint;
        this.upperConstraint = upperConstraint;
    }

    @Override
    public Object get() {
        return new StringBuilder().append(fieldName)
                                  .append(" BETWEEN '")
                                  .append(lowerConstraint)
                                  .append("' AND '")
                                  .append(upperConstraint)
                                  .append("'")
                                  .toString();
    }

}
