package mg.util.db.persist.field;

import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Int;

public class IntBuilder extends FieldBuilder {

    public IntBuilder(Persistable parentObject, Field declaredField, Int annotation) {
        super(parentObject, declaredField, annotation);

        notNull = annotation.notNull();
    }

    @Override
    public String build() {

        return new StringBuilder(name).append(" INT")
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

}
