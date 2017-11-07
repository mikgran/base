package mg.util.db.persist.field;

import java.lang.reflect.Field;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.DateTime;
import mg.util.db.persist.annotation.Decimal;
import mg.util.db.persist.annotation.ForeignKey;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Int;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.OneToOne;
import mg.util.db.persist.annotation.VarChar;

// TOIMPROVE: replace with (?) HashMap<Annotation, builderCreateFunction<BuilderParameters, Builder>> -> return hashMap.get(annotation).apply(builderParameters);
public class FieldBuilderFactory {

    public static <T extends Persistable> FieldBuilder of(T parentObject, Field declaredFieldOfParentObject) {

        VarChar varCharAnnotation = declaredFieldOfParentObject.getAnnotation(VarChar.class);
        if (varCharAnnotation != null) {
            return new VarCharBuilder(parentObject, declaredFieldOfParentObject, varCharAnnotation);
        }

        OneToMany oneToManyAnnotation = declaredFieldOfParentObject.getAnnotation(OneToMany.class);
        if (oneToManyAnnotation != null) {
            OneToManyBuilder oneToManyBuilder = new OneToManyBuilder(parentObject, declaredFieldOfParentObject, oneToManyAnnotation);
            if (oneToManyBuilder.isOneToManyField()) {
                return oneToManyBuilder;
            } else {
                return new NonBuilder(parentObject, declaredFieldOfParentObject, oneToManyAnnotation);
            }
        }

        OneToOne oneToOneAnnotation = declaredFieldOfParentObject.getAnnotation(OneToOne.class);
        if (oneToOneAnnotation != null) {
            OneToOneBuilder oneToOneBuilder = new OneToOneBuilder(parentObject, declaredFieldOfParentObject, oneToOneAnnotation);
            if (oneToOneBuilder.isOneToOneField()) {
                return oneToOneBuilder;
            } else {
                return new NonBuilder(parentObject, declaredFieldOfParentObject, oneToOneAnnotation);
            }
        }

        DateTime dateTimeAnnotation = declaredFieldOfParentObject.getAnnotation(DateTime.class);
        if (dateTimeAnnotation != null) {
            return new DateTimeBuilder(parentObject, declaredFieldOfParentObject, dateTimeAnnotation);
        }

        Int intAnnotation = declaredFieldOfParentObject.getAnnotation(Int.class);
        if (intAnnotation != null) {
            return new IntBuilder(parentObject, declaredFieldOfParentObject, intAnnotation);
        }

        Decimal decimalAnnotation = declaredFieldOfParentObject.getAnnotation(Decimal.class);
        if (decimalAnnotation != null) {
            return new DecimalBuilder(parentObject, declaredFieldOfParentObject, decimalAnnotation);
        }

        ForeignKey foreignKeyAnnotation = declaredFieldOfParentObject.getAnnotation(ForeignKey.class);
        if (foreignKeyAnnotation != null) {
            return new ForeignKeyBuilder(parentObject, declaredFieldOfParentObject, foreignKeyAnnotation);
        }

        Id idAnnotation = declaredFieldOfParentObject.getAnnotation(Id.class);
        if (idAnnotation != null) {
            return new IdBuilder(parentObject, declaredFieldOfParentObject, idAnnotation);
        }

        return new NonBuilder(parentObject, declaredFieldOfParentObject, null);
    }
}
