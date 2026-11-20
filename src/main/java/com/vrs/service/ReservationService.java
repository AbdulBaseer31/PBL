package com.vrs.service;

import com.vrs.dao.CustomerDao;
import com.vrs.dao.ReservationDao;
import com.vrs.dao.VehicleDao;
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
public class ReservationService {

    private final ReservationDao reservationDao;
    private final VehicleDao vehicleDao;
    private final CustomerDao customerDao;

    public ReservationService(ReservationDao reservationDao, VehicleDao vehicleDao, CustomerDao customerDao) {
        this.reservationDao = reservationDao;
        this.vehicleDao = vehicleDao;
        this.customerDao = customerDao;
    }

    public Reservation createReservation(String numberplate, LocalDate reservDate, int reservPeriod,
                                         String requestedCustomerId, CurrentUser user) {
        if (numberplate == null || numberplate.isBlank() || reservDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required reservation fields");
        }
        if (reservDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation date cannot be in the past");
        }
        if (reservPeriod < 1 || reservPeriod > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental period must be between 1 and 30 days");
        }

        // Admin can specify customerId; Customer always uses their own token ID
        String effectiveCustomerId = user.isAdmin() && requestedCustomerId != null && !requestedCustomerId.isBlank()
                ? requestedCustomerId.trim()
                : user.getCustomerId();

        try {
            if (customerDao.findById(effectiveCustomerId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            if (vehicleDao.findByNumberplate(numberplate.trim()) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
            }
            if (reservationDao.hasOverlap(numberplate.trim(), reservDate, reservPeriod)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle is already reserved for those dates");
            }

            String reservationId = IdGenerator.next("R");
            Reservation reservation = new Reservation(reservationId, reservDate, reservPeriod, numberplate.trim(), effectiveCustomerId);
            try {
                reservationDao.insert(reservation);
            } catch (SQLException e) {
                reservationId = IdGenerator.next("R");
                reservation = new Reservation(reservationId, reservDate, reservPeriod, numberplate.trim(), effectiveCustomerId);
                reservationDao.insert(reservation);
            }
            return reservation;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error creating reservation");
        }
    }

    public List<Reservation> getReservations(CurrentUser user) {
        try {
            if (user.isAdmin()) {
                return reservationDao.findAll();
            }
            return reservationDao.findByCustomer(user.getCustomerId());
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error fetching reservations");
        }
    }

    public Reservation getReservationById(String reservationId, CurrentUser user) {
        try {
            Reservation reservation = reservationDao.findById(reservationId);
            if (reservation == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
            }
            // Ownership rule: Return 404 if a customer tries to access another customer's reservation
            if (!user.isAdmin() && !reservation.getCustomerId().equals(user.getCustomerId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
            }
            return reservation;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error fetching reservation");
        }
    }
}

