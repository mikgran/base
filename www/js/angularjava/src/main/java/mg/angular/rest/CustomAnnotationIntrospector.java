package mg.angular.rest;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import mg.util.db.persist.Persistable;

public class CustomAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private static final long serialVersionUID = 27077710012017L;

    @Override
    public Object findFilterId(Annotated ac) {

        // default to super behavior.
        Object id = super.findFilterId(ac); // TOCONSIDER: remove this?

        // and if not found, use 'defaultFilter'
        if (id == null) {
            id = "defaultFilter";
        }
        return id;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember annotatedMember) {

        // filter out Persistable class fields always.
        return annotatedMember.getDeclaringClass() == Persistable.class ||
               super.hasIgnoreMarker(annotatedMember);
    }

}
