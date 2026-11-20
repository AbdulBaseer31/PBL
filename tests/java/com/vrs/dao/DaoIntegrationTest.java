package com.vrs.dao;

import com.vrs.model.Bill;
import com.vrs.model.Customer;
import com.vrs.model.Reservation;
import com.vrs.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DaoIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private VehicleDao vehicleDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private BillDao billDao;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Bill");
            stmt.executeUpdate("DELETE FROM Reservation");
            stmt.executeUpdate("DELETE FROM Vehicle");
            stmt.executeUpdate("DELETE FROM Customer");
        }
    }

    @Test
    void customerDao_insertAndFind() throws SQLException {
        Customer c = new Customer("C100001", "Alice", 25, "Hyderabad", "9876543210", "hash123", "CUSTOMER");
        customerDao.insert(c);

        Customer found = customerDao.findById("C100001");
        assertNotNull(found);
        assertEquals("Alice", found.getName());

        Customer byContact = customerDao.findByContactNumber("9876543210");
        assertNotNull(byContact);
        assertEquals("C100001", byContact.getCustomerId());

        assertEquals(1, customerDao.countByRole("CUSTOMER"));
    }

    @Test
    void vehicleDao_insertSearchAndDelete() throws SQLException {
        Vehicle v1 = new Vehicle("TS01AA1111", "Sedan", "Honda", new BigDecimal("2000.00"));
        Vehicle v2 = new Vehicle("TS01BB2222", "SUV", "Toyota", new BigDecimal("3500.00"));
        vehicleDao.insert(v1);
        vehicleDao.insert(v2);

        assertEquals(2, vehicleDao.countAll());

        List<Vehicle> sedans = vehicleDao.search("Sedan", null, null, false);
        assertEquals(1, sedans.size());
        assertEquals("TS01AA1111", sedans.get(0).getNumberplate());

        List<Vehicle> under2500 = vehicleDao.search(null, null, new BigDecimal("2500.00"), false);
        assertEquals(1, under2500.size());

        assertTrue(vehicleDao.delete("TS01AA1111"));
        assertNull(vehicleDao.findByNumberplate("TS01AA1111"));
    }

    @Test
    void reservationDao_overlapDetection() throws SQLException {
        Customer c = new Customer("C100002", "Bob", 30, "Delhi", "9876543211", "hash123", "CUSTOMER");
        customerDao.insert(c);

        Vehicle v = new Vehicle("TS02CC3333", "SUV", "Ford", new BigDecimal("3000.00"));
        vehicleDao.insert(v);

        // Reserved from 2026-12-10 to 2026-12-15
        Reservation r = new Reservation("R100001", LocalDate.of(2026, 12, 10), 5, "TS02CC3333", "C100002");
        reservationDao.insert(r);

        // Overlapping range (2026-12-12 for 3 days) -> true
        assertTrue(reservationDao.hasOverlap("TS02CC3333", LocalDate.of(2026, 12, 12), 3));

        // Back-to-back start on 2026-12-15 -> false
        assertFalse(reservationDao.hasOverlap("TS02CC3333", LocalDate.of(2026, 12, 15), 3));

        // Completely separate range -> false
        assertFalse(reservationDao.hasOverlap("TS02CC3333", LocalDate.of(2026, 12, 20), 2));
    }

    @Test
    void billDao_joinComputesTotalCost() throws SQLException {
        Customer c = new Customer("C100003", "Charlie", 28, "Mumbai", "9876543212", "hash123", "CUSTOMER");
        customerDao.insert(c);

        Vehicle v = new Vehicle("TS03DD4444", "Sedan", "Hyundai", new BigDecimal("1500.00"));
        vehicleDao.insert(v);

        Reservation r = new Reservation("R100002", LocalDate.of(2026, 11, 10), 4, "TS03DD4444", "C100003");
        reservationDao.insert(r);

        Bill b = new Bill("B100001", LocalDate.of(2026, 11, 11), "TS03DD4444", "C100003", "R100002");
        billDao.insert(b);

        assertTrue(billDao.existsForReservation("R100002"));

        Bill.Detail detail = billDao.findDetailById("B100001");
        assertNotNull(detail);
        assertEquals("Charlie", detail.getCustomerName());
        assertEquals("Hyundai", detail.getBrand());
        assertEquals(4, detail.getDays());
        // 1500 * 4 = 6000
        assertEquals(0, new BigDecimal("6000.00").compareTo(detail.getTotalCost()));
    }
}

