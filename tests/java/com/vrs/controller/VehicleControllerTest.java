package com.vrs.controller;

import com.vrs.model.Vehicle;
import com.vrs.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {VehicleController.class, ApiExceptionHandler.class})
public class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @Test
    void searchVehicles_returnsList() throws Exception {
        Vehicle v = new Vehicle("TS09AB1234", "Sedan", "Honda", new BigDecimal("2500.00"));
        when(vehicleService.searchVehicles(any(), any(), any(), any(Boolean.class)))
                .thenReturn(List.of(v));

        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numberplate").value("TS09AB1234"));
    }

    @Test
    void getStats_returnsCounts() throws Exception {
        when(vehicleService.getStats()).thenReturn(Map.of(
                "totalVehicles", 14,
                "activeReservations", 5,
                "unreservedVehicles", 9
        ));

        mockMvc.perform(get("/vehicles/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVehicles").value(14))
                .andExpect(jsonPath("$.unreservedVehicles").value(9));
    }

    @Test
    void addVehicle_createsSuccessfully() throws Exception {
        Vehicle v = new Vehicle("TS09AB1234", "Sedan", "Honda", new BigDecimal("2500.00"));
        when(vehicleService.addVehicle(anyString(), anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(v);

        String json = """
                {
                  "numberplate": "TS09AB1234",
                  "type": "Sedan",
                  "brand": "Honda",
                  "basePrice": 2500.00
                }
                """;

        mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numberplate").value("TS09AB1234"));
    }

    @Test
    void removeVehicle_conflictWhenUsed() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle has reservations or bills and cannot be removed"))
                .when(vehicleService).removeVehicle("TS09AB1234");

        mockMvc.perform(delete("/vehicles/TS09AB1234"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Vehicle has reservations or bills and cannot be removed"));
    }
}

