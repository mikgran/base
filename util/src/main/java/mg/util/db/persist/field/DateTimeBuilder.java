package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import mg.util.db.persist.Persistable;

public class DateTimeBuilder extends FieldBuilder {

    public DateTimeBuilder(Persistable parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);

        // TODO: DateTimeBuilder: constructor deny usage outside Date, LocalDateTime, LocalDate and Timestamp
//        if (declaredField.) {
//
//        }
    }

    @Override
    public String build() {

        return new StringBuilder(name).append(" DATETIME NOT NULL")
                                      .toString();
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
    public void setFieldValue(Object value) {
        try {
            if (value != null) {
                if (value instanceof LocalDateTime || value instanceof Date) {

                    assignReflectedFieldValue(value);

                } else if (value instanceof Timestamp) {

                    //assignReflectedFieldValue(wrapInReflectedType(value));
                    // TODO: DateTimeBuilder: setFieldValue
                    assignReflectedFieldValue(value);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // this should never happen
            logger.error(format("Object Type %s, field named %s, declaredField.set(parent, object) failed with:\n%s", parentObject.getClass(), declaredField.getName(),
                                e.getMessage()));
        }
    }

    private Object wrapInReflectedType(Object value) {

        Class<?> type = declaredField.getType();



        return null;
    }

    private void assignReflectedFieldValue(Object value) throws IllegalAccessException {
        declaredField.setAccessible(true);
        declaredField.set(parentObject, value);
    }

}
