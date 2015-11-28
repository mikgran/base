package mg.util.db.persist.field;

import static java.lang.String.format;

import java.lang.reflect.Field;

import mg.util.db.persist.annotation.OneToMany;

public class CollectionBuilder extends FieldBuilder {

    public CollectionBuilder(Object parentObject, Field declaredField, OneToMany annotation) {
        super(parentObject, declaredField, annotation);

        name = declaredField.getName();
        value = getFieldValue(parentObject, declaredField);

        logger.debug("field value type: " + (value != null ? value.getClass().getSimpleName() : "<no type>"));

        sql = "[NYI]";
    }

    @Override
    public boolean isDbField() {
        return false;
    }

    @Override
    public String toString() {
        return format("[name: %s, value: %s, sql: %s]", name, value.toString(), sql);
    }

}
