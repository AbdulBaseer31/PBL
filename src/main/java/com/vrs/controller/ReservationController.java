package com.vrs.controller;

import com.vrs.model.Reservation;
import com.vrs.security.CurrentUser;
import com.vrs.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public static class ReservationRequest {
        public String numberplate;
        public LocalDate reservDate;
        public int reservPeriod;
        public String customerId;
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest req,
                                                         HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        Reservation reservation = reservationService.createReservation(
                req.numberplate, req.reservDate, req.reservPeriod, req.customerId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations(HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        return ResponseEntity.ok(reservationService.getReservations(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable String id,
                                                          HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        return ResponseEntity.ok(reservationService.getReservationById(id, user));
    }
}

