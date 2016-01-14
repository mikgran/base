package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.util.Collection;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.OneToMany;

public class CollectionBuilder extends FieldBuilder {

    public CollectionBuilder(Persistable parentObject, Field declaredField, OneToMany annotation) {
        super(parentObject, declaredField, annotation);
    }

    @Override
    public String build() {
        return "[N/A]";
    }

    @Override
    public boolean isCollectionField() {
        return value != null && value instanceof Collection;
    }

    @Override
    public boolean isDbField() {
        return false;
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
    public void setFieldValue(Object parentObject, Object value) {
        try {
            if (parentObject != null &&
                this.parentObject.getClass().equals(parentObject.getClass()) &&
                value != null && value instanceof Collection) {
                declaredField.setAccessible(true);
                declaredField.set(parentObject, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // this should never happen
            logger.error(format("Object Type %s, field named %s, declaredField.set(parent, object) failed with:\n%s", parentObject.getClass(), declaredField.getName(),
                                e.getMessage()));
        }
    }

    @Override
    public String toString() {
        return format("[name: %s, value: %s, sql: %s]", name, value.toString(), build());
    }

}
