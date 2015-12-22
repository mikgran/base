package mg.util.db.persist.constraint;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DateLaterConstraint extends Constraint {

    private LocalDateTime localDateTime;

    public DateLaterConstraint(String fieldName, LocalDateTime localDateTime) {
        super(fieldName);
        this.localDateTime = localDateTime;
    }

    @Override
    public Object get() {
        return new StringBuilder().append(fieldName)
                                  .append(" >= '")
                                  .append(Timestamp.valueOf(localDateTime))
                                  .append("'")
                                  .toString();
    }

}
