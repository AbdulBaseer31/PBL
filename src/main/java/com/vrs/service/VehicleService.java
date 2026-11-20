package com.vrs.service;

import com.vrs.dao.VehicleDao;
import com.vrs.model.Vehicle;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VehicleService {

    private static final Set<String> ALLOWED_TYPES = Set.of("Sedan", "SUV", "Hatchback", "Bike", "Scooter");
    private final VehicleDao vehicleDao;

    public VehicleService(VehicleDao vehicleDao) {
        this.vehicleDao = vehicleDao;
    }

    public List<Vehicle> searchVehicles(String type, String brand, BigDecimal maxPrice, boolean availableOnly) {
        return searchVehicles(type, brand, maxPrice, availableOnly, null);
    }

    public List<Vehicle> searchVehicles(String type, String brand, BigDecimal maxPrice, boolean availableOnly, String sortBy) {
        try {
            return vehicleDao.search(type, brand, maxPrice, availableOnly, sortBy);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while searching vehicles");
        }
    }

    public Vehicle addVehicle(String numberplate, String type, String brand, BigDecimal basePrice) {
        if (numberplate == null || numberplate.isBlank() ||
            type == null || type.isBlank() ||
            brand == null || brand.isBlank() ||
            basePrice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All fields are required");
        }
        if (!ALLOWED_TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vehicle type: " + type);
        }
        if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Base price must be greater than 0");
        }

        try {
            if (vehicleDao.findByNumberplate(numberplate.trim()) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Numberplate already exists");
            }
            Vehicle vehicle = new Vehicle(numberplate.trim(), type, brand.trim(), basePrice);
            vehicleDao.insert(vehicle);
            return vehicle;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while adding vehicle");
        }
    }

    public void removeVehicle(String numberplate) {
        try {
            Vehicle vehicle = vehicleDao.findByNumberplate(numberplate);
            if (vehicle == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
            }
            if (vehicleDao.isUsed(numberplate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle has reservations or bills and cannot be removed");
            }
            vehicleDao.delete(numberplate);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while removing vehicle");
        }
    }

    public Map<String, Integer> getStats() {
        try {
            int total = vehicleDao.countAll();
            int activeReservations = vehicleDao.countActiveReservations();
            int withActiveRes = vehicleDao.countWithActiveReservation();
            int unreserved = total - withActiveRes;
            return Map.of(
                    "totalVehicles", total,
                    "activeReservations", activeReservations,
                    "unreservedVehicles", unreserved
            );
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while fetching vehicle stats");
        }
    }
}

