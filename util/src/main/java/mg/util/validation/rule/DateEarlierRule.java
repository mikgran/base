package mg.util.validation.rule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import mg.util.Common;

/**
 * A validation rule for testing whether the dateAfter is later than given date and both are not null. <br />
 * Usage: new Validator().add("date", new Date(longToday), DATE_EARLIER.than(new Date(longTomorrow))).validate();
 */
public class DateEarlierRule extends ValidationRule {

    private LocalDateTime afterDate = null;

    public DateEarlierRule() {
    }

    private DateEarlierRule(LocalDateTime afterDate) {
        this.afterDate = afterDate;
    }

    private DateEarlierRule(Date afterDate) {
        this.afterDate = afterDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Tests whether the given object, which is assumed to be Date or LocalDate, is before the afterDate.
     */
    @Override
    public boolean apply(Object object) {

        if (object == null || afterDate == null) {
            return false;
        }

        if (object instanceof LocalDateTime && afterDate.isAfter((LocalDateTime) object)) {
            return true;
        }

        if (object instanceof Date && afterDate.isAfter(Common.toLocalDateTime((Date) object))) {
            return true;
        }
        return false;
    }

    @Override
    public String getMessage() {
        return "applied date can not be after than afterDate";
    }

    public DateEarlierRule than(Date afterDate) {
        return new DateEarlierRule(afterDate);
    }

    public DateEarlierRule than(LocalDateTime afterDate) {
        return new DateEarlierRule(afterDate);
    }

}
