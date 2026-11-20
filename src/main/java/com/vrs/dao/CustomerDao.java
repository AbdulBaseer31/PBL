package com.vrs.dao;

import com.vrs.model.Customer;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class CustomerDao {
    private final DataSource dataSource;

    public CustomerDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customer (CustomerID, Name, Age, Address, Contact_Number, Password, Role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getName());
            ps.setInt(3, customer.getAge());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getContactNumber());
            ps.setString(6, customer.getPassword());
            ps.setString(7, customer.getRole());
            ps.executeUpdate();
        }
    }

    public Customer findById(String customerId) throws SQLException {
        String sql = "SELECT CustomerID, Name, Age, Address, Contact_Number, Password, Role FROM Customer WHERE CustomerID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Customer findByContactNumber(String contactNumber) throws SQLException {
        String sql = "SELECT CustomerID, Name, Age, Address, Contact_Number, Password, Role FROM Customer WHERE Contact_Number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int countByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer WHERE Role = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("CustomerID"),
                rs.getString("Name"),
                rs.getInt("Age"),
                rs.getString("Address"),
                rs.getString("Contact_Number"),
                rs.getString("Password"),
                rs.getString("Role")
        );
    }
}

