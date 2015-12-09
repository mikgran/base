package mg.util.db.persist.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.VarChar;

/*
 * Remember to sync the ResultSetMapper for each type here.
 */
public class FieldBuilderFactory {

    public static FieldBuilder of(Object parentObject, Field declaredFieldOfParentObject) {

        Annotation[] annotations = declaredFieldOfParentObject.getAnnotations();

        // TOIMPROVE: add multiple annotation guard(s)
        for (Annotation annotation : annotations) {

            if (annotation instanceof VarChar) {

                // @VarChar
                // private String field;
                return new VarCharBuilder(parentObject, declaredFieldOfParentObject, (VarChar) annotation);

            } else if (annotation instanceof OneToMany) {

                // @OneToMany
                // private Collection field;
                CollectionBuilder collectionBuilder = new CollectionBuilder(parentObject, declaredFieldOfParentObject, (OneToMany) annotation);

                if (collectionBuilder.isCollectionField()) {

                    return collectionBuilder;

                } else {

                    return new NonBuilder(parentObject, declaredFieldOfParentObject, annotation);
                }

            }
        }

        return new NonBuilder(parentObject, declaredFieldOfParentObject, null);
    }

}
