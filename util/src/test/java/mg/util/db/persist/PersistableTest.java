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
import mg.util.db.persist.constraint.BetweenConstraintBuilder;
import mg.util.db.persist.constraint.ConstraintBuilder;
import mg.util.db.persist.constraint.DateBeforeConstraintBuilder;
import mg.util.db.persist.constraint.DateLaterConstraintBuilder;
import mg.util.db.persist.constraint.IsStringConstraintBuilder;
import mg.util.db.persist.constraint.LikeStringConstraintBuilder;
import mg.util.db.persist.support.Contact3;

public class PersistableTest {

    private static Connection connection;

    @BeforeClass
    public static void setupOnce() throws IOException {
        connection = TestDBSetup.setupDbAndGetConnection("dbotest");
    }

    @AfterClass
    public static void tearDownOnce() throws SQLException {
        Common.close(connection);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testConstraintBetween() {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("dateOfBirth")
               .between(LocalDateTime.of(2010, 10, 10, 12, 45),
                        LocalDateTime.of(2010, 10, 20, 14, 25));

        List<ConstraintBuilder> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<ConstraintBuilder> constraintsForNameField = constraints.stream()
                                                              .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                              .collect(Collectors.toList());

        assertEquals("there should be: ", 1, constraintsForNameField.size());
        ConstraintBuilder constraint = constraintsForNameField.get(0);
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
        List<ConstraintBuilder> constraintsForNameField = constraints.stream()
                                                              .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                              .collect(Collectors.toList());

        assertEquals("there should be constraintsForNameField: ", 1, constraintsForNameField.size());
        ConstraintBuilder constraint = constraintsForNameField.get(0);
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
        List<ConstraintBuilder> constraintsForNameField = constraints.stream()
                                                              .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                              .collect(Collectors.toList());

        assertEquals("there should be constraintsForNameField: ", 1, constraintsForNameField.size());
        ConstraintBuilder constraint = constraintsForNameField.get(0);
        assertTrue("there should be constraints for field 'dateOfBirth': ", constraint instanceof DateBeforeConstraintBuilder);
        assertEquals("constraint should be: ", "dateOfBirth <= '2010-10-10 12:45:00.0'", constraint.build());
        assertEquals("fieldName after constraint operation should be: ", "dateOfBirth", contact.getFieldName());
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

        assertEquals("there should be: ", 1, constraintsForNameField.size());
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

}
