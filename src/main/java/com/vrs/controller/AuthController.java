package com.vrs.controller;

import com.vrs.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public static class RegisterRequest {
        public String name;
        public int age;
        public String address;
        public String contactNumber;
        public String password;
    }

    public static class LoginRequest {
        public String contactNumber;
        public String password;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest req) {
        String customerId = authService.register(req.name, req.age, req.address, req.contactNumber, req.password);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("customerId", customerId));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Map<String, Object> result = authService.login(req.contactNumber, req.password);
        return ResponseEntity.ok(result);
    }
}

