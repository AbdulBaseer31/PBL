package com.vrs.service;

import com.vrs.dao.VehicleDao;
import com.vrs.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VehicleServiceTest {

    private VehicleDao vehicleDao;
    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleDao = mock(VehicleDao.class);
        vehicleService = new VehicleService(vehicleDao);
    }

    @Test
    void addVehicle_invalidType_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                vehicleService.addVehicle("TS01AA1111", "Truck", "Tata", new BigDecimal("1000")));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void addVehicle_duplicateNumberplate_throwsConflict() throws SQLException {
        when(vehicleDao.findByNumberplate("TS01AA1111")).thenReturn(new Vehicle("TS01AA1111", "Sedan", "Honda", new BigDecimal("1000")));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                vehicleService.addVehicle("TS01AA1111", "Sedan", "Honda", new BigDecimal("1000")));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void removeVehicle_notFound_throwsNotFound() throws SQLException {
        when(vehicleDao.findByNumberplate("TS01AA9999")).thenReturn(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                vehicleService.removeVehicle("TS01AA9999"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void removeVehicle_vehicleHasReservationsOrBills_throwsConflict() throws SQLException {
        when(vehicleDao.findByNumberplate("TS01AA1111")).thenReturn(new Vehicle("TS01AA1111", "Sedan", "Honda", new BigDecimal("1000")));
        when(vehicleDao.isUsed("TS01AA1111")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                vehicleService.removeVehicle("TS01AA1111"));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("has reservations or bills"));
    }

    @Test
    void getStats_computesCorrectUnreservedCount() throws SQLException {
        when(vehicleDao.countAll()).thenReturn(14);
        when(vehicleDao.countActiveReservations()).thenReturn(5);
        when(vehicleDao.countWithActiveReservation()).thenReturn(5);

        Map<String, Integer> stats = vehicleService.getStats();
        assertEquals(14, stats.get("totalVehicles"));
        assertEquals(5, stats.get("activeReservations"));
        assertEquals(9, stats.get("unreservedVehicles"));
    }
}

