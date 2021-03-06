package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.VarChar;

public class VarCharBuilder extends FieldBuilder {

    public VarCharBuilder(Persistable parentObject, Field declaredField, VarChar annotation) {
        super(parentObject, declaredField, annotation);

        length = annotation.length();
        notNull = annotation.notNull();
    }

    @Override
    public String build() {
        // i.e. email VARCHAR (40) NOT NULL,
        return new StringBuilder(name).append("")
                                      .append(" VARCHAR(")
                                      .append(length)
                                      .append(")")
                                      .append(notNull ? " NOT NULL" : "")
                                      .toString();
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
    public boolean isOneToManyField() {
        return false;
    }

    @Override
    public boolean isOneToOneField() {
        return false;
    }

    @Override
    public boolean isPrimaryKeyField() {
        return false;
    }

    @Override
    public void setFieldValue(Object parentObject, Object value) {
        try {
            if (parentObject != null && compareTypeToParentType(parentObject) &&
                value != null && value instanceof String) {

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
