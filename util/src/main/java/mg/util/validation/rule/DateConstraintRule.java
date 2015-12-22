package mg.util.validation.rule;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import mg.util.Common;

public class DateConstraintRule extends ValidationRule {

    private LocalDateTime lowerConstraint = null;
    private LocalDateTime upperConstraint = null;

    public DateConstraintRule() {
    }

    public DateConstraintRule(Date lowerConstraint, Date upperConstraint) {
        requireNonNullConstraints(lowerConstraint, upperConstraint);
        this.lowerConstraint = Common.toLocalDateTime(lowerConstraint);
        this.upperConstraint = Common.toLocalDateTime(upperConstraint);
    }

    public DateConstraintRule(LocalDateTime lowerConstraint, LocalDateTime upperConstraint) {
        requireNonNullConstraints(lowerConstraint, upperConstraint);
        this.lowerConstraint = lowerConstraint;
        this.upperConstraint = upperConstraint;
    }

    @Override
    public boolean apply(Object object) {

        if (object != null && object instanceof LocalDateTime) {

            LocalDateTime timeCandidate = (LocalDateTime) object;

            return isBetweenConstraints(timeCandidate);

        } else if (object != null && object instanceof Date) {

            LocalDateTime timeCandidate = Common.toLocalDateTime((Date) object);

            return isBetweenConstraints(timeCandidate);
        }

        return false;
    }

    public DateConstraintRule forDates(String lowerConstraint, String upperConstraint) throws ParseException {
        Date lowerBoundary = Common.yyyyMMddHHmmFormatter.parse(lowerConstraint);
        Date upperBoundary = Common.yyyyMMddHHmmFormatter.parse(upperConstraint);
        return new DateConstraintRule(lowerBoundary, upperBoundary);
    }

    @Override
    public String getMessage() {
        return "can not be empty or null string.";
    }

    private boolean isBetweenConstraints(LocalDateTime timeCandidate) {
        if (timeCandidate.isAfter(lowerConstraint) &&
            timeCandidate.isBefore(upperConstraint)) {
            return true;
        }
        return false;
    }

    private void requireNonNullConstraints(Object lowerConstraint, Object upperConstraint) {
        Objects.requireNonNull(lowerConstraint, "lowerConstraint can not be null.");
        Objects.requireNonNull(upperConstraint, "upperConstraint can not be null.");
    }

}
