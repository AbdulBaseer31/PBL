package com.vrs.controller;

import com.vrs.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, ApiExceptionHandler.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void register_returns201WithCustomerId() throws Exception {
        when(authService.register(anyString(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn("C482910");

        String body = """
                {
                  "name": "Avanish Reddy",
                  "age": 20,
                  "address": "Hyderabad",
                  "contactNumber": "9876543210",
                  "password": "secret123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("C482910"));
    }

    @Test
    void login_wrongCredentials_returns401() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong contact number or password"));

        String body = """
                {
                  "contactNumber": "9876543210",
                  "password": "wrong"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Wrong contact number or password"));
    }
}

