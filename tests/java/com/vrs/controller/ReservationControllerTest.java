package com.vrs.controller;

import com.vrs.model.Reservation;
import com.vrs.security.CurrentUser;
import com.vrs.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ReservationController.class, ApiExceptionHandler.class})
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Test
    void createReservation_returnsCreated() throws Exception {
        Reservation r = new Reservation("R103377", LocalDate.of(2026, 12, 1), 5, "TS09AB1234", "C482910");
        when(reservationService.createReservation(anyString(), any(LocalDate.class), anyInt(), any(), any()))
                .thenReturn(r);

        String json = """
                {
                  "numberplate": "TS09AB1234",
                  "reservDate": "2026-12-01",
                  "reservPeriod": 5
                }
                """;

        mockMvc.perform(post("/reservations")
                        .requestAttr("currentUser", new CurrentUser("C482910", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value("R103377"))
                .andExpect(jsonPath("$.numberplate").value("TS09AB1234"));
    }

    @Test
    void getReservations_returnsList() throws Exception {
        Reservation r = new Reservation("R103377", LocalDate.of(2026, 12, 1), 5, "TS09AB1234", "C482910");
        when(reservationService.getReservations(any())).thenReturn(List.of(r));

        mockMvc.perform(get("/reservations")
                        .requestAttr("currentUser", new CurrentUser("C482910", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value("R103377"));
    }
}

