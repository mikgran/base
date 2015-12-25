package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import mg.util.Common;

public class DateTimeBuilder extends FieldBuilder {

    public DateTimeBuilder(Object parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);

        name = declaredField.getName();
        value = getFieldValue(parentObject, declaredField);

        logger.debug(toString());
    }

    @Override
    public String build() {

        if (value != null && value instanceof LocalDateTime) {

            return Timestamp.valueOf((LocalDateTime)value).toString();

        } else if (value != null && value instanceof Date) {

            LocalDateTime localDateTime = Common.toLocalDateTime((Date)value);
            return Timestamp.valueOf(localDateTime).toString();
        }
        return "";
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
    public void setFieldValue(Object value) {
        try {
            if (value != null && (value instanceof LocalDateTime || value instanceof Date)) {
                declaredField.setAccessible(true);
                declaredField.set(parentObject, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // this should never happen
            logger.error(format("Object Type %s, field named %s, declaredField.set(parent, object) failed with:\n%s", parentObject.getClass(), declaredField.getName(),
                                e.getMessage()));
        }
    }

}
