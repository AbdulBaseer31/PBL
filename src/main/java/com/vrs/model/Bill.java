package com.vrs.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Bill {
    private String billId;
    private LocalDate billDate;
    private String numberplate;
    private String customerId;
    private String reservationId;

    public Bill(String billId, LocalDate billDate, String numberplate, String customerId, String reservationId) {
        this.billId = billId;
        this.billDate = billDate;
        this.numberplate = numberplate;
        this.customerId = customerId;
        this.reservationId = reservationId;
    }

    public String getBillId() { return billId; }
    public LocalDate getBillDate() { return billDate; }
    public String getNumberplate() { return numberplate; }
    public String getCustomerId() { return customerId; }
    public String getReservationId() { return reservationId; }

    public static class Detail {
        private String billId;
        private LocalDate billDate;
        private String reservationId;
        private String customerId;
        private String customerName;
        private String numberplate;
        private String brand;
        private BigDecimal basePrice;
        private int days;
        private BigDecimal totalCost;

        public Detail(String billId, LocalDate billDate, String reservationId, String customerId,
                      String customerName, String numberplate, String brand, BigDecimal basePrice,
                      int days, BigDecimal totalCost) {
            this.billId = billId;
            this.billDate = billDate;
            this.reservationId = reservationId;
            this.customerId = customerId;
            this.customerName = customerName;
            this.numberplate = numberplate;
            this.brand = brand;
            this.basePrice = basePrice;
            this.days = days;
            this.totalCost = totalCost;
        }

        public String getBillId() { return billId; }
        public LocalDate getBillDate() { return billDate; }
        public String getReservationId() { return reservationId; }
        public String getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public String getNumberplate() { return numberplate; }
        public String getBrand() { return brand; }
        public BigDecimal getBasePrice() { return basePrice; }
        public int getDays() { return days; }
        public BigDecimal getTotalCost() { return totalCost; }
    }
}

