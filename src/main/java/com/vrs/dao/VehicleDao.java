package com.vrs.dao;

import com.vrs.model.Vehicle;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VehicleDao {
    private final DataSource dataSource;

    public VehicleDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO Vehicle (Numberplate, Type, Brand, BasePrice) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicle.getNumberplate());
            ps.setString(2, vehicle.getType());
            ps.setString(3, vehicle.getBrand());
            ps.setBigDecimal(4, vehicle.getBasePrice());
            ps.executeUpdate();
        }
    }

    public Vehicle findByNumberplate(String numberplate) throws SQLException {
        String sql = "SELECT Numberplate, Type, Brand, BasePrice FROM Vehicle WHERE Numberplate = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numberplate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Vehicle> search(String type, String brand, BigDecimal maxPrice, boolean availableOnly) throws SQLException {
        return search(type, brand, maxPrice, availableOnly, null);
    }

    public List<Vehicle> search(String type, String brand, BigDecimal maxPrice, boolean availableOnly, String sortBy) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT v.Numberplate, v.Type, v.Brand, v.BasePrice FROM Vehicle v WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isBlank()) {
            sql.append("AND v.Type = ? ");
            params.add(type);
        }
        if (brand != null && !brand.isBlank()) {
            sql.append("AND v.Brand LIKE ? ");
            params.add("%" + brand.trim() + "%");
        }
        if (maxPrice != null) {
            sql.append("AND v.BasePrice <= ? ");
            params.add(maxPrice);
        }
        if (availableOnly) {
            sql.append("AND v.Numberplate NOT IN (")
               .append("SELECT r.Numberplate FROM Reservation r ")
               .append("WHERE DATE_ADD(r.ReservDate, INTERVAL r.ReservPeriod DAY) > CURRENT_DATE()) ");
        }

        if ("alpha".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY v.Brand ASC");
        } else if ("year_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY CAST(SUBSTRING(v.Brand, -4) AS UNSIGNED) DESC, v.Brand ASC");
        } else if ("year_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY CAST(SUBSTRING(v.Brand, -4) AS UNSIGNED) ASC, v.Brand ASC");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY v.BasePrice DESC");
        } else if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY v.BasePrice ASC");
        } else {
            // Default: Randomized display
            sql.append("ORDER BY RAND(42)");
        }

        List<Vehicle> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean delete(String numberplate) throws SQLException {
        String sql = "DELETE FROM Vehicle WHERE Numberplate = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numberplate);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicle";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countWithActiveReservation() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT Numberplate) FROM Reservation WHERE DATE_ADD(ReservDate, INTERVAL ReservPeriod DAY) > CURRENT_DATE()";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countActiveReservations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE DATE_ADD(ReservDate, INTERVAL ReservPeriod DAY) > CURRENT_DATE()";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean isUsed(String numberplate) throws SQLException {
        String sql = "SELECT 1 FROM Reservation WHERE Numberplate = ? UNION SELECT 1 FROM Bill WHERE Numberplate = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numberplate);
            ps.setString(2, numberplate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getString("Numberplate"),
                rs.getString("Type"),
                rs.getString("Brand"),
                rs.getBigDecimal("BasePrice")
        );
    }
}

