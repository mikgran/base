package mg.util.db.persist.field;

import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.ForeignKey;

public class ForeignKeyBuilder extends FieldBuilder {

    private String references;
    private int referencedId;

    public ForeignKeyBuilder(Persistable parentObject, Field declaredField, ForeignKey annotation) {
        super(parentObject, declaredField, annotation);

        references = validateContent(annotation.references(), "References value has no content.");
        referencedId = parentObject.getId();

        setFieldValue(referencedId);
    }

    @Override
    public String build() {

        // the MEDIUMINT type must match the id field in ForeignKey.references(id)
        return new StringBuilder(name).append(" MEDIUMINT NOT NULL")
                                      .toString();
    }

    @Override
    public String buildForeignKey() {

        return new StringBuilder("FOREIGN KEY ").append("(")
                                                .append(name)
                                                .append(") REFERENCES ")
                                                .append(references)
                                                .append("(id)")
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
        return true;
    }

}
