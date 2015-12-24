package mg.util.db.persist.field;

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

}
