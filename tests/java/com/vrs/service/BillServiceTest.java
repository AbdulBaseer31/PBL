package com.vrs.service;

import com.vrs.dao.BillDao;
import com.vrs.dao.ReservationDao;
import com.vrs.model.Bill;
import com.vrs.model.Reservation;
import com.vrs.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BillServiceTest {

    private BillDao billDao;
    private ReservationDao reservationDao;
    private BillService billService;

    @BeforeEach
    void setUp() {
        billDao = mock(BillDao.class);
        reservationDao = mock(ReservationDao.class);
        billService = new BillService(billDao, reservationDao);
    }

    @Test
    void createBill_reservationNotFound_throws404() throws SQLException {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        when(reservationDao.findById("R999")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                billService.createBill("R999", user));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void createBill_otherCustomerReservation_throws404() throws SQLException {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        Reservation r = new Reservation("R1", LocalDate.now(), 3, "TS01AA1111", "C2");
        when(reservationDao.findById("R1")).thenReturn(r);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                billService.createBill("R1", user));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void createBill_alreadyBilled_throws409() throws SQLException {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        Reservation r = new Reservation("R1", LocalDate.now(), 3, "TS01AA1111", "C1");
        when(reservationDao.findById("R1")).thenReturn(r);
        when(billDao.existsForReservation("R1")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                billService.createBill("R1", user));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already exists"));
    }

    @Test
    void createBill_success() throws SQLException {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        Reservation r = new Reservation("R1", LocalDate.now(), 3, "TS01AA1111", "C1");
        when(reservationDao.findById("R1")).thenReturn(r);
        when(billDao.existsForReservation("R1")).thenReturn(false);

        Bill.Detail mockDetail = new Bill.Detail(
                "B1001", LocalDate.now(), "R1", "C1", "Alice",
                "TS01AA1111", "Honda", new BigDecimal("1000"), 3, new BigDecimal("3000")
        );
        when(billDao.findDetailById(anyString())).thenReturn(mockDetail);

        Bill.Detail result = billService.createBill("R1", user);
        assertNotNull(result);
        assertEquals("B1001", result.getBillId());
        assertEquals(new BigDecimal("3000"), result.getTotalCost());
    }

    @Test
    void getBillById_otherCustomer_returns404() throws SQLException {
        CurrentUser user = new CurrentUser("C1", "CUSTOMER");
        Bill.Detail detail = new Bill.Detail(
                "B1001", LocalDate.now(), "R1", "C2", "Bob",
                "TS01AA1111", "Honda", new BigDecimal("1000"), 3, new BigDecimal("3000")
        );
        when(billDao.findDetailById("B1001")).thenReturn(detail);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                billService.getBillById("B1001", user));
        assertEquals(404, ex.getStatusCode().value());
    }
}

