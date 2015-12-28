package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class IntBuilder extends FieldBuilder {

    public IntBuilder(Object parentObject, Field declaredField, Annotation annotation) {
        super(parentObject, declaredField, annotation);


    }

    @Override
    public String build() {

        return new StringBuilder(name).append(" INT ")
                                      .append("")
                                      .toString();
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

}
