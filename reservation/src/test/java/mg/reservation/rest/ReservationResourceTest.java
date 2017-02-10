package mg.reservation.rest;

import static mg.reservation.common.DBConstants.RESERVATIONS_TEST_DATA_INSERT;
import static mg.reservation.common.DBConstants.RESERVATIONS_TEST_DB_CREATE;
import static mg.reservation.common.DBConstants.RESERVATIONS_TEST_DB_DROP;
import static mg.util.Common.close;
import static mg.util.Common.getDateFrom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mg.reservation.common.TestUtil;
import mg.reservation.db.Reservation;
import mg.reservation.service.ReservationServiceImpl;
import mg.util.TestConfig;
import mg.util.db.DBConfig;
import mg.util.db.TestDBSetup;

public class ReservationResourceTest extends TestUtil {

    private static ReservationServiceImpl reservationService = null;

    private static String dbName = "jdbc:mysql://localhost/reservationtest2";
    private static DBConfig dbConfig = null;
    private static Connection connection = null;
    private static ReservationResource testResource = null;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // 'DD', 'Beta', 'person', '2014-05-26 12:00', '2014-05-26 13:00', 'title4',
    // 'desc4'
    private Reservation expectedReservation = new Reservation("DD", "Beta", "person", getDateFrom("1401094800000"), getDateFrom("1401098400000"), "title4", "desc4");

    @BeforeClass
    public static void setupOnce() throws IOException {
        dbConfig = new DBConfig(new TestConfig(dbName));
        reservationService = new ReservationServiceImpl(dbConfig);
        connection = TestDBSetup.setupDbAndGetConnection(dbName, RESERVATIONS_TEST_DB_DROP, RESERVATIONS_TEST_DB_CREATE, RESERVATIONS_TEST_DATA_INSERT);
        testResource = new ReservationResource(reservationService);
    }

    @AfterClass
    public static void tearDownOnce() {
        close(connection);
    }

    @Test
    public void testListBadRequestNotNumberParameters() {
        thrown.expect(WebApplicationException.class);
        thrown.expectMessage("HTTP 400 Bad Request");
        testResource.listReservations("A", "B");
    }

    @Test
    public void testListBadRequestNullParameters() {
        thrown.expect(WebApplicationException.class);
        thrown.expectMessage("HTTP 400 Bad Request");
        testResource.listReservations((String) null, (String) null);
    }

    @Test
    public void testListBadRequestTooShortUnixDates() {
        thrown.expect(WebApplicationException.class);
        thrown.expectMessage("HTTP 400 Bad Request");
        testResource.listReservations("0", "1");
    }

    @Test
    public void testListNoContent() {
        thrown.expect(WebApplicationException.class);
        thrown.expectMessage("HTTP 204 No Content");
        testResource.listReservations("1400094800000", "1400098400000");
    }

    @Test
    public void testListReservations() throws Exception {

        List<Reservation> reservations = testResource.listReservations("1401094800000", "1401098400000");
        assertNotNull(reservations);
        assertEquals("there should be reservations", 1, reservations.size());
        assertTrue(listContains(reservations, expectedReservation));
    }

}
