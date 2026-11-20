package com.vrs.security;

public class CurrentUser {
    private final String customerId;
    private final String role;

    public CurrentUser(String customerId, String role) {
        this.customerId = customerId;
        this.role = role;
    }

    public String getCustomerId() { return customerId; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
}

