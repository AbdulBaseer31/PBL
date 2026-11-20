package com.vrs.service;

import com.vrs.dao.CustomerDao;
import com.vrs.model.Customer;
import com.vrs.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private CustomerDao customerDao;
    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        customerDao = mock(CustomerDao.class);
        jwtUtil = new JwtUtil("this_is_a_secret_key_with_at_least_32_characters_12345", 60);
        authService = new AuthService(customerDao, jwtUtil);
    }

    @Test
    void register_hashesPasswordAndSaves() throws SQLException {
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        String id = authService.register("John Doe", 25, "Hyderabad", "9876543210", "password123");

        assertNotNull(id);
        verify(customerDao, times(1)).insert(captor.capture());
        Customer saved = captor.getValue();
        assertNotEquals("password123", saved.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("password123", saved.getPassword()));
        assertEquals("CUSTOMER", saved.getRole());
    }

    @Test
    void register_underage_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.register("John", 17, "City", "9876543210", "password123"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void register_duplicateContact_throwsConflict() throws SQLException {
        when(customerDao.findByContactNumber("9876543210")).thenReturn(new Customer("C1", "A", 20, "City", "9876543210", "h", "CUSTOMER"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.register("John", 25, "City", "9876543210", "password123"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void login_wrongCredentials_throwsSame401Message() throws SQLException {
        when(customerDao.findByContactNumber("9999999999")).thenReturn(null);
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () ->
                authService.login("9999999999", "secret"));

        String hash = new BCryptPasswordEncoder().encode("correctPassword");
        Customer customer = new Customer("C1", "John", 25, "City", "9876543210", hash, "CUSTOMER");
        when(customerDao.findByContactNumber("9876543210")).thenReturn(customer);

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () ->
                authService.login("9876543210", "wrongPassword"));

        assertEquals(ex1.getReason(), ex2.getReason());
        assertEquals("Wrong contact number or password", ex1.getReason());
    }

    @Test
    void login_success_returnsValidTokenAndUser() throws SQLException {
        String hash = new BCryptPasswordEncoder().encode("secret123");
        Customer customer = new Customer("C123456", "Alice", 25, "City", "9876543210", hash, "CUSTOMER");
        when(customerDao.findByContactNumber("9876543210")).thenReturn(customer);

        Map<String, Object> result = authService.login("9876543210", "secret123");
        assertNotNull(result.get("token"));
        assertEquals("C123456", result.get("customerId"));
        assertEquals("Alice", result.get("name"));
        assertEquals("CUSTOMER", result.get("role"));
    }
}

