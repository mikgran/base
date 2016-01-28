package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;

public class OneToOneBuilder extends FieldBuilder {

    public OneToOneBuilder(Persistable parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);
    }

    @Override
    public String build() {
        return null;
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
        return Persistable.class.isAssignableFrom(declaredField.getType());
    }

    @Override
    public boolean isPrimaryKeyField() {
        return false;
    }

}
