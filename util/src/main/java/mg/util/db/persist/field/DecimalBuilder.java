package mg.util.db.persist.field;

import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Decimal;

public class DecimalBuilder extends FieldBuilder {

    public DecimalBuilder(Persistable parentObject, Field declaredField, Decimal annotation) {
        super(parentObject, declaredField, annotation);

        notNull = annotation.notNull();
    }

    @Override
    public String build() {

        return new StringBuilder(name).append(" DECIMAL (")
                                      .append("")
                                      .append("")
                                      .append("")
                                      .append(notNull ? " NOT NULL" : "")
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
