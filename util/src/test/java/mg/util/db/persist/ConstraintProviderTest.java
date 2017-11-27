package mg.util.db.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Collectors;

import org.junit.Test;

import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.support.Contact3;

public class ConstraintProviderTest {

    @Test
    public void testSetFieldsAsConstraints() throws Exception {
        try {
            String name = "name1234";
            String email = "email1234@comp.com";
            String phone = "222-2222-2222221234";
            Persistable p = new Contact3(0, name, email, phone);

            p.clearConstraints()
             .field("email").is(email)
             .field("name").is(name)
             .field("phone").is(phone);

            String constraints1 = buildConstraints(p);

            FieldBuilderCache builderCache = new FieldBuilderCache();
            BuilderInfo contact3BuilderInfo = builderCache.buildersFor(new Contact3());

            p.clearConstraints()
             .setConstraints(persistable -> {
                 contact3BuilderInfo.fieldBuilders.stream()
                                                  .filter(fb -> !fb.isIdField())
                                                  .filter(fb -> fb.getDeclaredField().getType().isAssignableFrom(String.class))
                                                  .sorted((a, b) -> a.getDeclaredField()
                                                                     .getName()
                                                                     .compareToIgnoreCase(b.getDeclaredField()
                                                                                           .getName()))
                                                  .forEach(fb -> {
                                                      String fieldName = fb.getName();
                                                      String fieldValue = fb.getFieldValue(persistable).toString();
                                                      persistable.field(fieldName)
                                                                 .is(fieldValue);
                                                  });
             });

            String constraints2 = buildConstraints(p);
            assertEquals(constraints1, constraints2);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    private String buildConstraints(Persistable p) {
        return p.getConstraints()
                .stream()
                .map(ConstraintBuilder::build)
                .collect(Collectors.joining(" "));
    }

}
