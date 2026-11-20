package com.vrs.dao;

import com.vrs.model.Bill;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BillDao {
    private final DataSource dataSource;

    public BillDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Bill bill) throws SQLException {
        String sql = "INSERT INTO Bill (BillID, BillDate, Numberplate, CustomerID, ReservationID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bill.getBillId());
            ps.setDate(2, Date.valueOf(bill.getBillDate()));
            ps.setString(3, bill.getNumberplate());
            ps.setString(4, bill.getCustomerId());
            ps.setString(5, bill.getReservationId());
            ps.executeUpdate();
        }
    }

    public boolean existsForReservation(String reservationId) throws SQLException {
        String sql = "SELECT 1 FROM Bill WHERE ReservationID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Bill.Detail findDetailById(String billId) throws SQLException {
        String sql = baseJoinSql() + "WHERE b.BillID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDetailRow(rs);
                }
            }
        }
        return null;
    }

    public List<Bill.Detail> findDetailsByCustomer(String customerId) throws SQLException {
        String sql = baseJoinSql() + "WHERE b.CustomerID = ? ORDER BY b.BillDate DESC";
        List<Bill.Detail> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDetailRow(rs));
                }
            }
        }
        return list;
    }

    public List<Bill.Detail> findAllDetails() throws SQLException {
        String sql = baseJoinSql() + "ORDER BY b.BillDate DESC";
        List<Bill.Detail> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDetailRow(rs));
            }
        }
        return list;
    }

    private String baseJoinSql() {
        return "SELECT b.BillID, b.BillDate, b.ReservationID, b.CustomerID, c.Name AS CustomerName, " +
               "v.Numberplate, v.Brand, v.BasePrice, r.ReservPeriod AS Days, " +
               "(v.BasePrice * r.ReservPeriod) AS TotalCost " +
               "FROM Bill b " +
               "JOIN Customer c ON b.CustomerID = c.CustomerID " +
               "JOIN Vehicle v ON b.Numberplate = v.Numberplate " +
               "JOIN Reservation r ON b.ReservationID = r.ReservationID ";
    }

    private Bill.Detail mapDetailRow(ResultSet rs) throws SQLException {
        return new Bill.Detail(
                rs.getString("BillID"),
                rs.getDate("BillDate").toLocalDate(),
                rs.getString("ReservationID"),
                rs.getString("CustomerID"),
                rs.getString("CustomerName"),
                rs.getString("Numberplate"),
                rs.getString("Brand"),
                rs.getBigDecimal("BasePrice"),
                rs.getInt("Days"),
                rs.getBigDecimal("TotalCost")
        );
    }
}

