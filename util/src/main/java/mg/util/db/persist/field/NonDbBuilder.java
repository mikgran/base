package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Field is a non table field: a transitive field. Building from a non db field
 * is not possible. getSql() will yield a null.
 */
public class NonDbBuilder extends FieldBuilder {

    public NonDbBuilder(Object parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);
        
        name = declaredField.getName();
        value = getFieldValue(parentObject, declaredField);
        sql = "[not buildable into SQL]";
    }

    /**
     * The field can not be built into SQL.
     */
    @Override
    public boolean isDbField() {
        return false;
    }
}
