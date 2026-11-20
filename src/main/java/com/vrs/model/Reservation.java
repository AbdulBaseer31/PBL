package com.vrs.model;

import java.time.LocalDate;

public class Reservation {
    private String reservationId;
    private LocalDate reservDate;
    private int reservPeriod;
    private String numberplate;
    private String customerId;

    public Reservation(String reservationId, LocalDate reservDate, int reservPeriod, String numberplate, String customerId) {
        this.reservationId = reservationId;
        this.reservDate = reservDate;
        this.reservPeriod = reservPeriod;
        this.numberplate = numberplate;
        this.customerId = customerId;
    }

    public String getReservationId() { return reservationId; }
    public LocalDate getReservDate() { return reservDate; }
    public int getReservPeriod() { return reservPeriod; }
    public String getNumberplate() { return numberplate; }
    public String getCustomerId() { return customerId; }
}

