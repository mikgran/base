package mg.util.db.persist.field;

import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;

public class IdBuilder extends FieldBuilder {

    private boolean autoIncrement;

    public IdBuilder(Persistable parentObject, Field declaredField, Id annotation) {
        super(parentObject, declaredField, annotation);

        autoIncrement = annotation.autoincrement();

        // TOCONSIDER: allow int type fields?
        disallowIntFieldType(declaredField);
    }

    @Override
    public String build() {

        return new StringBuilder(name).append(" BIGINT NOT NULL")
                                      .append(autoIncrement ? " AUTO_INCREMENT" : "")
                                      .append(" PRIMARY KEY")
                                      .toString();
    }

    @Override
    public String buildForeignKey() {
        return "";
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

    @Override
    public boolean isIdField() {
        return true;
    }
}
