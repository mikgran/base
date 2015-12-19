package mg.util.db.persist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.util.Common;
import mg.util.db.TestDBSetup;
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
    public void testConstraints() throws Exception {

        /*
            TODO: 
                - dates: later than, before than
                - ids: specific, need for ranges?
            i.e.
            String name = "";
            String email = "";
            String phone = "";

            - Persistable contains HashesTable -> each field hash value
            - Contact.field("name") <- points to String name
                     .like("sam") <- adds Constraint.LIKE.for("sam")
                     .is("sam") <- adds Constraint.IS.for("sam")
                     .field("start") <- now points to Date start
                     .after(getDateTwoWeeksAgo()) <- adds Constraint.DATE_AFTER.for("10.10.2015")
                     .field("end") <- now points to Date end
                     .before(new Date()); <- adds Constraint.DATE_BEFORE.for("20.10.2015")
                     .between(getDateTenDaysAgo(), new Date()); <- replaces the current constraint for the start with WHERE start BETWEEN '10.10.2015' AND '20.10.2015'; 
           Contact contact = DB.findBy(Contact) <- checks for constraints, if any present: builds a SELECT * FROM contacts, WHERE name like "sam" AND start >= '10.10.2015' AND end <= '20.10.2015' 
         */

        Persistable contact = new Contact3(0, "name", "email@comp.com", "111-1111-11111");

        contact.field("name")
               .is("firstName LastName");

    }

}
