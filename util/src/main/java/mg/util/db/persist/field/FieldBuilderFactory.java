package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.DateTime;
import mg.util.db.persist.annotation.Decimal;
import mg.util.db.persist.annotation.ForeignKey;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Int;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.VarChar;

public class FieldBuilderFactory {

    public static <T extends Persistable> FieldBuilder of(T parentObject, Field declaredFieldOfParentObject) {

        Annotation[] annotations = declaredFieldOfParentObject.getAnnotations();

        // TOIMPROVE: add multiple annotation guard(s) and/or change to process multiple annotations for a field
        // TOIMPROVE: add type mapping from all PRIMITIVES into SQL TYPES into field builders: int -> fieldName MEDIUMINT NOT NULL, float -> ...
        for (Annotation annotation : annotations) {

            if (annotation instanceof VarChar) {

                return new VarCharBuilder(parentObject, declaredFieldOfParentObject, (VarChar) annotation);

            } else if (annotation instanceof OneToMany) {

                CollectionBuilder collectionBuilder = new CollectionBuilder(parentObject, declaredFieldOfParentObject, (OneToMany) annotation);

                if (collectionBuilder.isCollectionField(parentObject)) {

                    return collectionBuilder;
                } else {

                    return new NonBuilder(parentObject, declaredFieldOfParentObject, annotation);
                }
            } else if (annotation instanceof DateTime) {

                return new DateTimeBuilder(parentObject, declaredFieldOfParentObject, (DateTime) annotation);

            } else if (annotation instanceof Int) {

                return new IntBuilder(parentObject, declaredFieldOfParentObject, (Int) annotation);

            } else if (annotation instanceof Decimal) {

                return new DecimalBuilder(parentObject, declaredFieldOfParentObject, (Decimal) annotation);

            } else if (annotation instanceof ForeignKey) {

                return new ForeignKeyBuilder(parentObject, declaredFieldOfParentObject, (ForeignKey) annotation);

            } else if (annotation instanceof Id) {

                return new IdBuilder(parentObject, declaredFieldOfParentObject, (Id) annotation);
            }

        }

        return new NonBuilder(parentObject, declaredFieldOfParentObject, null);
    }

}
