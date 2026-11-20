package com.vrs.model;

import java.math.BigDecimal;

public class Vehicle {
    private String numberplate;
    private String type;
    private String brand;
    private BigDecimal basePrice;

    public Vehicle(String numberplate, String type, String brand, BigDecimal basePrice) {
        this.numberplate = numberplate;
        this.type = type;
        this.brand = brand;
        this.basePrice = basePrice;
    }

    public String getNumberplate() { return numberplate; }
    public String getType() { return type; }
    public String getBrand() { return brand; }
    public BigDecimal getBasePrice() { return basePrice; }
}

