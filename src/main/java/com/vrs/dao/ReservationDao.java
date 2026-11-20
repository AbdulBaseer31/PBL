package com.vrs.dao;

import com.vrs.model.Reservation;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReservationDao {
    private final DataSource dataSource;

    public ReservationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO Reservation (ReservationID, ReservDate, ReservPeriod, Numberplate, CustomerID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservation.getReservationId());
            ps.setDate(2, Date.valueOf(reservation.getReservDate()));
            ps.setInt(3, reservation.getReservPeriod());
            ps.setString(4, reservation.getNumberplate());
            ps.setString(5, reservation.getCustomerId());
            ps.executeUpdate();
        }
    }

    public Reservation findById(String reservationId) throws SQLException {
        String sql = "SELECT ReservationID, ReservDate, ReservPeriod, Numberplate, CustomerID FROM Reservation WHERE ReservationID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Reservation> findByCustomer(String customerId) throws SQLException {
        String sql = "SELECT ReservationID, ReservDate, ReservPeriod, Numberplate, CustomerID FROM Reservation WHERE CustomerID = ? ORDER BY ReservDate DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT ReservationID, ReservDate, ReservPeriod, Numberplate, CustomerID FROM Reservation ORDER BY ReservDate DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean hasOverlap(String numberplate, LocalDate startDate, int period) throws SQLException {
        LocalDate endDate = startDate.plusDays(period);
        String sql = "SELECT 1 FROM Reservation WHERE Numberplate = ? " +
                     "AND ? < DATE_ADD(ReservDate, INTERVAL ReservPeriod DAY) " +
                     "AND ? > ReservDate LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numberplate);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getString("ReservationID"),
                rs.getDate("ReservDate").toLocalDate(),
                rs.getInt("ReservPeriod"),
                rs.getString("Numberplate"),
                rs.getString("CustomerID")
        );
    }
}

