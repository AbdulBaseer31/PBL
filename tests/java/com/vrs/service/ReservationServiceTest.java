package com.vrs.service;

import com.vrs.dao.CustomerDao;
import com.vrs.dao.ReservationDao;
import com.vrs.dao.VehicleDao;
import com.vrs.model.Customer;
import com.vrs.model.Reservation;
import com.vrs.model.Vehicle;
import com.vrs.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    private ReservationDao reservationDao;
    private VehicleDao vehicleDao;
    private CustomerDao customerDao;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationDao = mock(ReservationDao.class);
        vehicleDao = mock(VehicleDao.class);
        customerDao = mock(CustomerDao.class);
        reservationService = new ReservationService(reservationDao, vehicleDao, customerDao);
    }

    @Test
    void createReservation_pastDate_throwsBadRequest() {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                reservationService.createReservation("TS01AA1111", pastDate, 5, null, user));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("past"));
    }

    @Test
    void createReservation_invalidPeriod_throwsBadRequest() {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                reservationService.createReservation("TS01AA1111", futureDate, 0, null, user));
        assertEquals(400, ex.getStatusCode().value());

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () ->
                reservationService.createReservation("TS01AA1111", futureDate, 31, null, user));
        assertEquals(400, ex2.getStatusCode().value());
    }

    @Test
    void createReservation_overlap_throwsConflict() throws SQLException {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        LocalDate futureDate = LocalDate.now().plusDays(2);
        when(customerDao.findById("C1")).thenReturn(new Customer("C1", "A", 20, "City", "9999999999", "h", "CUSTOMER"));
        when(vehicleDao.findByNumberplate("TS01AA1111")).thenReturn(new Vehicle("TS01AA1111", "Sedan", "Honda", new BigDecimal("1000")));
        when(reservationDao.hasOverlap("TS01AA1111", futureDate, 5)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                reservationService.createReservation("TS01AA1111", futureDate, 5, null, user));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already reserved"));
    }

    @Test
    void createReservation_customerIgnoresRequestedCustomerId() throws SQLException {
        CurrentUser customerUser = new CurrentUser("C_REAL", "CUSTOMER");
        LocalDate futureDate = LocalDate.now().plusDays(2);
        when(customerDao.findById("C_REAL")).thenReturn(new Customer("C_REAL", "A", 20, "City", "9999999999", "h", "CUSTOMER"));
        when(vehicleDao.findByNumberplate("TS01AA1111")).thenReturn(new Vehicle("TS01AA1111", "Sedan", "Honda", new BigDecimal("1000")));
        when(reservationDao.hasOverlap("TS01AA1111", futureDate, 3)).thenReturn(false);

        Reservation created = reservationService.createReservation("TS01AA1111", futureDate, 3, "C_SPOOFED", customerUser);
        assertEquals("C_REAL", created.getCustomerId());
    }

    @Test
    void getReservationById_differentCustomer_returns404() throws SQLException {
        CurrentUser customerUser = new CurrentUser("C1", "CUSTOMER");
        Reservation otherReservation = new Reservation("R1", LocalDate.now(), 2, "TS01AA1111", "C2");
        when(reservationDao.findById("R1")).thenReturn(otherReservation);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                reservationService.getReservationById("R1", customerUser));
        assertEquals(404, ex.getStatusCode().value());
    }
}

