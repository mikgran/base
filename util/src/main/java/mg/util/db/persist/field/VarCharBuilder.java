package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.reflect.Field;

import mg.util.db.persist.annotation.VarChar;

public class VarCharBuilder extends FieldBuilder {

    public VarCharBuilder(Object parentObject, Field declaredField, VarChar annotation) {
        super(parentObject, declaredField, annotation);

        name = declaredField.getName();
        length = annotation.length();
        notNull = annotation.notNull();
        value = getFieldValue(parentObject, declaredField);

        // i.e. email VARCHAR (40) NOT NULL,
        sql = format("%s VARCHAR(%s) %s", name, length, (notNull ? "NOT NULL" : ""));

        logger.debug(toString());
    }

    @Override
    public boolean isDbField() {
        return true;
    }

    @Override
    public boolean isCollectionField() {
        return false;
    }

    @Override
    public void setFieldValue(Object value) {
        try {
            if (value instanceof String) {
                declaredField.setAccessible(true);
                declaredField.set(parentObject, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // this should never happen
            logger.error(format("Object Type %s, field named %s, declaredField.set(parent, object) failed with:\n%s", parentObject.getClass(), declaredField.getName(), e.getMessage()));
        }
    }
}
