package com.vrs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtFilterTest {

    private JwtUtil jwtUtil;
    private JwtFilter jwtFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("this_is_a_secret_key_with_at_least_32_characters_12345", 60);
        jwtFilter = new JwtFilter(jwtUtil);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void publicAuthEndpoint_passesWithoutToken() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/auth/login");
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(req, res, filterChain);

        verify(filterChain, times(1)).doFilter(req, res);
        assertEquals(200, res.getStatus());
    }

    @Test
    void protectedEndpoint_withoutHeader_returns401() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/vehicles");
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(req, res, filterChain);

        verify(filterChain, never()).doFilter(req, res);
        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("Login required"));
    }

    @Test
    void protectedEndpoint_withInvalidToken_returns401() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/vehicles");
        req.addHeader("Authorization", "Bearer invalid.token.payload");
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(req, res, filterChain);

        verify(filterChain, never()).doFilter(req, res);
        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("Invalid token"));
    }

    @Test
    void protectedEndpoint_withValidToken_passesAndSetsUser() throws ServletException, IOException {
        String token = jwtUtil.createToken("C123456", "CUSTOMER");
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/vehicles");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(req, res, filterChain);

        verify(filterChain, times(1)).doFilter(req, res);
        CurrentUser user = (CurrentUser) req.getAttribute("currentUser");
        assertNotNull(user);
        assertEquals("C123456", user.getCustomerId());
        assertEquals("CUSTOMER", user.getRole());
    }

    @Test
    void customerRole_accessingAdminRoute_returns403() throws ServletException, IOException {
        String token = jwtUtil.createToken("C123456", "CUSTOMER");
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/vehicles");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(req, res, filterChain);

        verify(filterChain, never()).doFilter(req, res);
        assertEquals(403, res.getStatus());
        assertTrue(res.getContentAsString().contains("Admin only"));
    }

    @Test
    void adminRole_accessingAdminRoute_passes() throws ServletException, IOException {
        String token = jwtUtil.createToken("C999999", "ADMIN");
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/vehicles");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(req, res, filterChain);

        verify(filterChain, times(1)).doFilter(req, res);
        assertEquals(200, res.getStatus());
    }
}

