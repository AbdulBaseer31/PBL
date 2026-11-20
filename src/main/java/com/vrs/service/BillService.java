package com.vrs.service;

import com.vrs.dao.BillDao;
import com.vrs.dao.ReservationDao;
import com.vrs.model.Bill;
import com.vrs.model.Reservation;
import com.vrs.security.CurrentUser;
import com.vrs.util.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {

    private final BillDao billDao;
    private final ReservationDao reservationDao;

    public BillService(BillDao billDao, ReservationDao reservationDao) {
        this.billDao = billDao;
        this.reservationDao = reservationDao;
    }

    public Bill.Detail createBill(String reservationId, CurrentUser user) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation ID is required");
        }

        try {
            Reservation reservation = reservationDao.findById(reservationId.trim());
            if (reservation == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
            }
            // Ownership rule: Customer can only bill their own reservation
            if (!user.isAdmin() && !reservation.getCustomerId().equals(user.getCustomerId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
            }
            if (billDao.existsForReservation(reservationId.trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Bill already exists for this reservation");
            }

            String billId = IdGenerator.next("B");
            Bill bill = new Bill(billId, LocalDate.now(), reservation.getNumberplate(),
                    reservation.getCustomerId(), reservation.getReservationId());
            try {
                billDao.insert(bill);
            } catch (SQLException e) {
                billId = IdGenerator.next("B");
                bill = new Bill(billId, LocalDate.now(), reservation.getNumberplate(),
                        reservation.getCustomerId(), reservation.getReservationId());
                billDao.insert(bill);
            }

            return billDao.findDetailById(billId);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error generating bill");
        }
    }

    public List<Bill.Detail> getBills(CurrentUser user) {
        try {
            if (user.isAdmin()) {
                return billDao.findAllDetails();
            }
            return billDao.findDetailsByCustomer(user.getCustomerId());
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error fetching bills");
        }
    }

    public Bill.Detail getBillById(String billId, CurrentUser user) {
        try {
            Bill.Detail detail = billDao.findDetailById(billId);
            if (detail == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
            }
            // Ownership rule: Return 404 if customer tries to view someone else's bill
            if (!user.isAdmin() && !detail.getCustomerId().equals(user.getCustomerId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
            }
            return detail;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error fetching bill");
        }
    }
}

