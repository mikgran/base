package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Field is a non table field: a transitive field. Building from a non db field
 * is not possible. Calling the build() will yield an empty String.
 */
public class NonBuilder extends FieldBuilder {

    public NonBuilder(Object parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);
    }

    @Override
    public String build() {
        return "";
    }

    @Override
    public boolean isCollectionField() {
        return false;
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
    public void setFieldValue(Object value) {
        value = "";
    }
}
