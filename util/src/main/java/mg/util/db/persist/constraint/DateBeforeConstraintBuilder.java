package mg.util.db.persist.constraint;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DateBeforeConstraintBuilder extends ConstraintBuilder {

    private LocalDateTime localDateTime;

    public DateBeforeConstraintBuilder(String fieldName, LocalDateTime localDateTime) {
        super(fieldName);
        this.localDateTime = localDateTime;
    }

    @Override
    public String build() {
        return new StringBuilder().append(fieldName)
                                  .append(" <= '")
                                  .append(Timestamp.valueOf(localDateTime))
                                  .append("'")
                                  .toString();
    }

}
