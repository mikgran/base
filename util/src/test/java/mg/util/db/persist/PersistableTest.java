package mg.util.db.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.Common;
import mg.util.db.TestDBSetup;
import mg.util.db.persist.constraint.AndConstraintBuilder;
import mg.util.db.persist.constraint.BetweenConstraintBuilder;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.constraint.DateBeforeConstraintBuilder;
import mg.util.db.persist.constraint.DateLaterConstraintBuilder;
import mg.util.db.persist.constraint.DecimalEqualsBuilder;
import mg.util.db.persist.constraint.GroupConstraintBuilder;
import mg.util.db.persist.constraint.IsStringConstraintBuilder;
import mg.util.db.persist.constraint.LikeStringConstraintBuilder;
import mg.util.db.persist.constraint.OrConstraintBuilder;
import mg.util.db.persist.support.Contact3;

public class PersistableTest {

    private static Connection connection;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupOnce() throws IOException {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        Common.close(connection);
    }

    // private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testConstraintBetween() {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("dateOfBirth")
               .between(LocalDateTime.of(2010, 10, 10, 12, 45),
                        LocalDateTime.of(2010, 10, 20, 14, 25));

        List<ConstraintBuilder> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<ConstraintBuilder> constraintsFordateOfBirthField = constraints.stream()
                                                                            .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                                            .collect(Collectors.toList());

        assertEquals("there should be constraintsFordateOfBirthField: ", 1, constraintsFordateOfBirthField.size());
        ConstraintBuilder constraint = constraintsFordateOfBirthField.get(0);
        assertTrue("there should be constraints for field 'dateOfBirth': ", constraint instanceof BetweenConstraintBuilder);
        assertEquals("constraint should be: ", "dateOfBirth BETWEEN '2010-10-10T12:45' AND '2010-10-20T14:25'", constraint.build());
        assertEquals("fieldName after constraint operation should be: ", "dateOfBirth", contact.getFieldName());
    }

    @Test
    public void testConstraintDateAfter() {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("dateOfBirth")
               .after(LocalDateTime.of(2010, 10, 10, 12, 45));

        List<ConstraintBuilder> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<ConstraintBuilder> constraintsFordateOfBirthField = constraints.stream()
                                                                            .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                                            .collect(Collectors.toList());

        assertEquals("there should be constraintsFordateOfBirthField: ", 1, constraintsFordateOfBirthField.size());
        ConstraintBuilder constraint = constraintsFordateOfBirthField.get(0);
        assertTrue("there should be constraints for field 'dateOfBirth': ", constraint instanceof DateLaterConstraintBuilder);
        assertEquals("constraint should be: ", "dateOfBirth >= '2010-10-10 12:45:00.0'", constraint.build());
        assertEquals("fieldName after constraint operation should be: ", "dateOfBirth", contact.getFieldName());
    }

    @Test
    public void testConstraintDateBefore() {
        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("dateOfBirth")
               .before(LocalDateTime.of(2010, 10, 10, 12, 45));

        List<ConstraintBuilder> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<ConstraintBuilder> constraintsFordateOfBirthField = constraints.stream()
                                                                            .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                                            .collect(Collectors.toList());

        assertEquals("there should be constraintsFordateOfBirthField: ", 1, constraintsFordateOfBirthField.size());
        ConstraintBuilder constraint = constraintsFordateOfBirthField.get(0);
        assertTrue("there should be constraints for field 'dateOfBirth': ", constraint instanceof DateBeforeConstraintBuilder);
        assertEquals("constraint should be: ", "dateOfBirth <= '2010-10-10 12:45:00.0'", constraint.build());
        assertEquals("fieldName after constraint operation should be: ", "dateOfBirth", contact.getFieldName());
    }

    @Test
    public void testConstraintIsDecimalLong() {

        Persistable contact = new Contact3(3, "name", "email@comp.com", "111-1111-11111");

        contact.field("id")
               .is(3L);

        List<ConstraintBuilder> constraints = contact.getConstraints();

        assertNotNull(constraints);
        System.out.println(constraints);
        List<ConstraintBuilder> constraintsForIdField = constraints.stream()
                                                                   .filter(constraint -> constraint.getFieldName().equals("id"))
                                                                   .collect(Collectors.toList());

        assertEquals("there should be constraint builders: ", 1, constraintsForIdField.size());
        ConstraintBuilder constraint = constraintsForIdField.get(0);
        assertTrue("there should be constraints for field 'id'", constraint instanceof DecimalEqualsBuilder);
    }

    @Test
    public void testConstraintIsString() throws Exception {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("name")
               .is("firstName LastName");

        List<ConstraintBuilder> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<ConstraintBuilder> constraintsForNameField = constraints.stream()
                                                                     .filter(constraint -> constraint.getFieldName().equals("name"))
                                                                     .collect(Collectors.toList());

        assertEquals("there should be constraint builders: ", 1, constraintsForNameField.size());
        ConstraintBuilder constraint = constraintsForNameField.get(0);
        assertTrue("there should be constraints for field 'name': ", constraint instanceof IsStringConstraintBuilder);
        assertEquals("constraint should be: ", "name = 'firstName LastName'", constraint.build());
        assertEquals("fieldName after constraint operation should be: ", "name", contact.getFieldName());
    }

    @Test
    public void testConstraintLike() {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("name")
               .like("firstName LastName");

        List<ConstraintBuilder> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<ConstraintBuilder> constraintsForNameField = constraints.stream()
                                                                     .filter(constraint -> constraint.getFieldName().equals("name"))
                                                                     .collect(Collectors.toList());

        assertEquals("there should be constraintsForNameField: ", 1, constraintsForNameField.size());
        ConstraintBuilder constraint = constraintsForNameField.get(0);
        assertTrue("there should be constraints for field 'name': ", constraint instanceof LikeStringConstraintBuilder);
        assertEquals("constraint should be: ", "name LIKE 'firstName LastName'", constraint.build());
        assertEquals("fieldName after constraint operation should be: ", "name", contact.getFieldName());
    }

    // XXX change the DSL for the queries: LAST LAST
    /*
     * contact.field("name").is("test testey")
     *        .or()                                     // instead of implicit conjunction opertor AND use OR
     *        .field("id").greaterThan(500)
     *        .group()                                  // all constraints zip up into a group.
     *        .field("phone").is("111 1111")            // after this there should be 1 group and 1 singular constraint
     *
     * should result in: SELECT * FROM contacts WHERE (name = "test testey" OR id > 500) AND (phone = "111 1111")
     *
     */
    // @Ignore
    @Test
    public void testGroupConstraints() {

        Persistable p = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        {
            p.field("name").is("test")
             .group();

            List<ConstraintBuilder> constraints = p.getConstraints();

            assertNotNull(constraints);
            assertEquals("there should be builders: ", 1, constraints.size());
            ConstraintBuilder constraintBuilder = constraints.get(0);
            assertEquals("there should be ", GroupConstraintBuilder.class, constraintBuilder.getClass());

            String constraintsString = constraintBuilder.build();
            assertEquals("group build should equal to: ", "(name = 'test')", constraintsString);
        }
        {
            p.or()
             .field("email").is("test2")
             .group();

            List<ConstraintBuilder> constraints = p.getConstraints();

            assertNotNull(constraints);
            assertEquals("there should be builders: ", 3, constraints.size()); // group1, or, group2 builders
            ConstraintBuilder constraintBuilder = constraints.get(0);
            assertEquals("there should be ", GroupConstraintBuilder.class, constraintBuilder.getClass());
            constraintBuilder = constraints.get(1);
            assertEquals("there should be ", OrConstraintBuilder.class, constraintBuilder.getClass());
            constraintBuilder = constraints.get(2);
            assertEquals("there should be ", GroupConstraintBuilder.class, constraintBuilder.getClass());

            String constraintsString = constraints.stream()
                                                  .map(ConstraintBuilder::build)
                                                  .collect(Collectors.joining(" "));
            assertEquals("group build should equal to: ", "(name = 'test') OR (email = 'test2')", constraintsString);
        }
        {
            p.clearConstraints()
             .field("name").is("test3") // XXX fix mee: terminal operator is() does not yet use or() / and() -> when changed all other relative tests fail asap.
             .or()
             .field("email").is("test4")
             .group()
             .and()
             .field("id").is(1L)
             .group();

            List<ConstraintBuilder> constraints = p.getConstraints();
            assertNotNull(constraints);
            assertEquals("there should be builders: ", 3, constraints.size()); // group1, or, group2 builders
            ConstraintBuilder constraintBuilder = constraints.get(0);
            assertEquals("there should be ", GroupConstraintBuilder.class, constraintBuilder.getClass());
            constraintBuilder = constraints.get(1);
            assertEquals("there should be ", AndConstraintBuilder.class, constraintBuilder.getClass());
            constraintBuilder = constraints.get(2);
            assertEquals("there should be ", GroupConstraintBuilder.class, constraintBuilder.getClass());

            String constraintsString = constraints.stream()
                                                  .map(ConstraintBuilder::build)
                                                  .collect(Collectors.joining(" "));
            assertEquals("group build should equal to: ", "(name = 'test3' OR email = 'test4') AND (id = '1')", constraintsString);
        }
    }

}
