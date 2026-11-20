package com.vrs.controller;

import com.vrs.model.Bill;
import com.vrs.security.CurrentUser;
import com.vrs.service.BillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BillController.class, ApiExceptionHandler.class})
public class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillService billService;

    @Test
    void createBill_returnsCreated() throws Exception {
        Bill.Detail detail = new Bill.Detail(
                "B552019", LocalDate.of(2026, 11, 11), "R103377", "C482910", "Avanish Reddy",
                "TS09AB1234", "Honda", new BigDecimal("2500.00"), 5, new BigDecimal("12500.00")
        );
        when(billService.createBill(anyString(), any())).thenReturn(detail);

        mockMvc.perform(post("/bills")
                        .requestAttr("currentUser", new CurrentUser("C482910", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reservationId\":\"R103377\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.billId").value("B552019"))
                .andExpect(jsonPath("$.totalCost").value(12500.00));
    }

    @Test
    void getBills_returnsList() throws Exception {
        Bill.Detail detail = new Bill.Detail(
                "B552019", LocalDate.of(2026, 11, 11), "R103377", "C482910", "Avanish Reddy",
                "TS09AB1234", "Honda", new BigDecimal("2500.00"), 5, new BigDecimal("12500.00")
        );
        when(billService.getBills(any())).thenReturn(List.of(detail));

        mockMvc.perform(get("/bills")
                        .requestAttr("currentUser", new CurrentUser("C482910", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].billId").value("B552019"));
    }
}

