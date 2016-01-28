package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;

/**
 * Field is a non table field: a transitive field. Building from a non db field
 * is not possible. Calling the build() will yield an empty String.
 */
public class NonBuilder extends FieldBuilder {

    public NonBuilder(Persistable parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);
    }

    @Override
    public String build() {
        return "";
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
    public boolean isOneToManyField() {
        return false;
    }

    @Override
    public boolean isOneToOneField() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPrimaryKeyField() {
        return false;
    }

    @Override
    public void setFieldValue(Object parentObject, Object value) {
        // no operation for the setFieldValue for transitive fields
    }
}
