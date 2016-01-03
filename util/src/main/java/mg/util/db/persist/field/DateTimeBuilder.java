package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import mg.util.Common;
import mg.util.db.persist.Persistable;

public class DateTimeBuilder extends FieldBuilder {

    public DateTimeBuilder(Persistable parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);

        Class<?> declaredFieldType = declaredField.getType();
        if (!(declaredFieldType == Date.class ||
              declaredFieldType == LocalDate.class ||
              declaredFieldType == LocalDateTime.class)) {

            throw new IllegalArgumentException("DateTime annotations can only be applied to Date, LocalDate or LocalDateTime fields.");
        }
    }

    @Override
    public String build() {

        return new StringBuilder(name).append(" DATETIME NOT NULL")
                                      .toString();
    }

    @Override
    public Object getFieldValue(Object parentObject, Field declaredField) {

        Object returnValue = super.getFieldValue(parentObject, declaredField);

        // attempt conversion, if unable to, return what we have, and let the
        // following exceptions break the program flow.
        if (returnValue != null) {
            if (returnValue instanceof LocalDateTime) {
                returnValue = Timestamp.valueOf((LocalDateTime) returnValue);
            } else if (returnValue instanceof Date) {
                returnValue = new Timestamp(((Date) returnValue).getTime());
            }
        }

        return returnValue;
    }

    @Override
    public boolean isCollectionField() {
        return false;
    }

    @Override
    public boolean isDbField() {
        return true;
    }

    @Override
    public boolean isForeignKeyField() {
        return false;
    }

    @Override
    public boolean isIdField() {
        return false;
    }

    @Override
    public boolean isPrimaryKeyField() {
        return false;
    }

    @Override
    public void setFieldValue(Object value) {

        // Hnngh what nonsense right here. Replace me with something better asap.
        Class<?> declaredFieldType = declaredField.getType();
        LocalDateTime newLocalDateTime = null;
        Object newValue = value;

        // attempt conversion, and should it fail: an exception will be produced via setFieldValue().
        // potential crash cases: after alter table column mismatch causes
        // setFieldValue(getObject(index)) to break the program. Perhaps change code to use partialMap
        // instead?
        if (value != null) {
            if (value instanceof Timestamp) {
                newLocalDateTime = ((Timestamp) value).toLocalDateTime();
            } else if (value instanceof Date) {
                newLocalDateTime = Common.toLocalDateTime((Date) value);
            } else if (value instanceof LocalDate) {
                newLocalDateTime = LocalDateTime.of(((LocalDate) value), LocalTime.MIDNIGHT);
            }
        }

        if (newLocalDateTime != null) {
            if (declaredFieldType == LocalDateTime.class) {
                newValue = newLocalDateTime;
            } else if (declaredFieldType == Date.class) {
                newValue = Common.toDate(newLocalDateTime);
            } else if (declaredFieldType == Timestamp.class) {
                newValue = Timestamp.valueOf(newLocalDateTime);
            }
        }

        super.setFieldValue(newValue);
    }

}
