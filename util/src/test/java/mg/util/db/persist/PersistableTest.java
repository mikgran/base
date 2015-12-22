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
import mg.util.db.persist.constraint.Constraint;
import mg.util.db.persist.constraint.DateLaterConstraint;
import mg.util.db.persist.constraint.IsConstraint;
import mg.util.db.persist.constraint.LikeConstraint;
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
    public void testConstraintDateAfter() {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("dateOfBirth")
               .after(LocalDateTime.of(2010, 10, 10, 12, 45));

        List<Constraint> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<Constraint> constraintsForNameField = constraints.stream()
                                                              .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                              .collect(Collectors.toList());

        assertEquals("there should be constraintsForNameField: ", 1, constraintsForNameField.size());
        Constraint constraint = constraintsForNameField.get(0);
        assertTrue("there should be constraints for field 'dateOfBirth': ", constraint instanceof DateLaterConstraint);
        assertEquals("constraint should be: ", "dateOfBirth >= '2010-10-10 12:45:00.0'", constraint.get());
        assertEquals("fieldName after constraint operation should be: ", "dateOfBirth", contact.getFieldName());
    }

    @Test
    public void testConstraintDateBefore() {
        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("dateOfBirth")
               .before(LocalDateTime.of(2010, 10, 10, 12, 45));

        List<Constraint> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<Constraint> constraintsForNameField = constraints.stream()
                                                              .filter(constraint -> constraint.getFieldName().equals("dateOfBirth"))
                                                              .collect(Collectors.toList());

        assertEquals("there should be constraintsForNameField: ", 1, constraintsForNameField.size());
        Constraint constraint = constraintsForNameField.get(0);
        assertTrue("there should be constraints for field 'dateOfBirth': ", constraint instanceof DateBeforeConstraint);
        assertEquals("constraint should be: ", "dateOfBirth <= '2010-10-10 12:45:00.0'", constraint.get());
        assertEquals("fieldName after constraint operation should be: ", "dateOfBirth", contact.getFieldName());
    }

    @Test
    public void testConstraintIsString() throws Exception {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("name")
               .is("firstName LastName");

        List<Constraint> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<Constraint> constraintsForNameField = constraints.stream()
                                                              .filter(constraint -> constraint.getFieldName().equals("name"))
                                                              .collect(Collectors.toList());

        assertEquals("there should be: ", 1, constraintsForNameField.size());
        Constraint constraint = constraintsForNameField.get(0);
        assertTrue("there should be constraints for field 'name': ", constraint instanceof IsConstraint);
        assertEquals("constraint should be: ", "name = 'firstName LastName'", constraint.get());
        assertEquals("fieldName after constraint operation should be: ", "name", contact.getFieldName());

    }

    /*
    TODO:
        - dates: later than, before than
        - ids: specific, need for ranges?
    i.e.
    String name = "";
    String email = "";
    String phone = "";

    - Persistable contains HashesTable -> each field hash value
    - Contact.field("xxx") <- points to String xxx
             .before(new Date()); <- adds Constraint.DATE_BEFORE.for("20.10.2015")
             .between(getDateTenDaysAgo(), new Date()); <- replaces the current constraint for the start with WHERE start BETWEEN '10.10.2015' AND '20.10.2015';
    Contact contact = DB.findBy(Contact) <- checks for constraints, if any present: builds a SELECT * FROM contacts, WHERE name like "sam" AND start >= '10.10.2015' AND end <= '20.10.2015'
    */

    @Test
    public void testConstraintLike() {

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("name")
               .like("firstName LastName");

        List<Constraint> constraints = contact.getConstraints();

        assertNotNull(constraints);
        List<Constraint> constraintsForNameField = constraints.stream()
                                                              .filter(constraint -> constraint.getFieldName().equals("name"))
                                                              .collect(Collectors.toList());

        assertEquals("there should be constraintsForNameField: ", 1, constraintsForNameField.size());
        Constraint constraint = constraintsForNameField.get(0);
        assertTrue("there should be constraints for field 'name': ", constraint instanceof LikeConstraint);
        assertEquals("constraint should be: ", "name LIKE 'firstName LastName'", constraint.get());
        assertEquals("fieldName after constraint operation should be: ", "name", contact.getFieldName());

    }

}
