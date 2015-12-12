package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Field is a non table field: a transitive field. Building from a non db field
 * is not possible. Calling the getSql() will yield a null.
 */
public class NonBuilder extends FieldBuilder {

    public NonBuilder(Object parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);

        name = declaredField.getName();
        value = null;
        sql = null;
    }

    /**
     * The field can not be built into SQL.
     */
    @Override
    public boolean isDbField() {
        return false;
    }

    @Override
    public boolean isCollectionField() {
        return false;
    }
}
