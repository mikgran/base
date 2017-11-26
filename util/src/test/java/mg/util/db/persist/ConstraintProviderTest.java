package mg.util.db.persist;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;

import org.junit.Test;

import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.support.Contact3;

public class ConstraintProviderTest {

    @Test
    public void testSetFieldsAsConstraints() {

        String name = "name222";
        String email = "email222@comp.com";
        String phone = "999-2222-22222";
        Persistable p = new Contact3(0, name, email, phone);

        p.clearConstraints()
         .field("name").is(name)
         .and()
         .field("email").is(email)
         .and()
         .field("phone").is(phone);

        String constraintsBuiltWithNormalMethd = buildConstraints(p);

        // String expectedConstraints = "name = 'name222' AND email = 'email222@comp.com' AND phone = '999-2222-22222'";

        p.clearConstraints()
         .setConstraints();

        String constraintsBuiltWithSetFieldsAsContraintsMethod = buildConstraints(p);

        assertEquals("the constraints built with normal and setFieldsAsConstraints methods should produce equal results: ", constraintsBuiltWithNormalMethd,
                     constraintsBuiltWithSetFieldsAsContraintsMethod);
    }

    private String buildConstraints(Persistable p) {
        return p.getConstraints()
                .stream()
                .map(ConstraintBuilder::build)
                .collect(Collectors.joining(" "));
    }

}
