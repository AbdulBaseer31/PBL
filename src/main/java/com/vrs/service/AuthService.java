package com.vrs.service;

import com.vrs.dao.CustomerDao;
import com.vrs.model.Customer;
import com.vrs.security.JwtUtil;
import com.vrs.util.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Map;

@Service
public class AuthService {

    private final CustomerDao customerDao;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(CustomerDao customerDao, JwtUtil jwtUtil) {
        this.customerDao = customerDao;
        this.jwtUtil = jwtUtil;
    }

    public String register(String name, int age, String address, String contactNumber, String password) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
        }
        if (age < 18 || age > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be between 18 and 100");
        }
        if (contactNumber == null || !contactNumber.matches("^\\d{10}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact number must be exactly 10 digits");
        }
        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        try {
            if (customerDao.findByContactNumber(contactNumber) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Contact number already registered");
            }
            String customerId = IdGenerator.next("C");
            String hashedPassword = passwordEncoder.encode(password);
            Customer customer = new Customer(customerId, name.trim(), age, address, contactNumber, hashedPassword, "CUSTOMER");
            try {
                customerDao.insert(customer);
            } catch (SQLException e) {
                // Retry once if duplicate ID generated
                customerId = IdGenerator.next("C");
                customer = new Customer(customerId, name.trim(), age, address, contactNumber, hashedPassword, "CUSTOMER");
                customerDao.insert(customer);
            }
            return customerId;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error during registration");
        }
    }

    public Map<String, Object> login(String contactNumber, String password) {
        if (contactNumber == null || password == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong contact number or password");
        }
        try {
            Customer customer = customerDao.findByContactNumber(contactNumber);
            if (customer == null || !passwordEncoder.matches(password, customer.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong contact number or password");
            }
            String token = jwtUtil.createToken(customer.getCustomerId(), customer.getRole());
            return Map.of(
                    "token", token,
                    "customerId", customer.getCustomerId(),
                    "name", customer.getName(),
                    "role", customer.getRole()
            );
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error during login");
        }
    }
}

