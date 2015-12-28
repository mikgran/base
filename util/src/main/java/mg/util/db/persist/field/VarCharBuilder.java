package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.reflect.Field;

import mg.util.db.persist.annotation.VarChar;

public class VarCharBuilder extends FieldBuilder {

    public VarCharBuilder(Object parentObject, Field declaredField, VarChar annotation) {
        super(parentObject, declaredField, annotation);

        length = validateContent(annotation.length(), "Length value has no content.");
        notNull = annotation.notNull();
    }

    @Override
    public String build() {
        // i.e. email VARCHAR (40) NOT NULL,
        return format("%s VARCHAR(%s) %s", name, length, (notNull ? "NOT NULL" : ""));
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
    public void setFieldValue(Object value) {
        try {
            if (value != null && value instanceof String) {
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
