package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.VarChar;

public class FieldBuilderFactory {

    public static FieldBuilder of(Object parentObject, Field declaredFieldOfParentObject) {

        Annotation[] annotations = declaredFieldOfParentObject.getAnnotations();

        // TOIMPROVE: add multiple annotation guard(s)
        for (Annotation annotation : annotations) {

            if (annotation instanceof VarChar) {

                return new VarCharBuilder(parentObject, declaredFieldOfParentObject, (VarChar) annotation);

            } else if (annotation instanceof OneToMany) {

                return new CollectionBuilder(parentObject, declaredFieldOfParentObject, (OneToMany) annotation);

            } else {

                return new NonDbBuilder(parentObject, declaredFieldOfParentObject, annotation);
            }

        }

        return null;
    }

}
