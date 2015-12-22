package mg.util.db.persist;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import mg.util.db.persist.constraint.Constraint;

public class DateBeforeConstraint extends Constraint {

    private LocalDateTime localDateTime;

    public DateBeforeConstraint(String fieldName, LocalDateTime localDateTime) {
        super(fieldName);
        this.localDateTime = localDateTime;
    }

    @Override
    public Object get() {
        return new StringBuilder().append(fieldName)
                                  .append(" <= '")
                                  .append(Timestamp.valueOf(localDateTime))
                                  .append("'")
                                  .toString();
    }

}
