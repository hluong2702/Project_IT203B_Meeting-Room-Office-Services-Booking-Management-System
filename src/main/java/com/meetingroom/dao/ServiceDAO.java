package com.meetingroom.dao;

import com.meetingroom.model.Service;
import com.meetingroom.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO thao tác với bảng services. */
public class ServiceDAO {

    private Service map(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setUnitPrice(rs.getDouble("unit_price"));
        s.setUnit(rs.getString("unit"));
        s.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }

    public boolean insert(Service sv) throws SQLException {
        String sql = "INSERT INTO services (name,unit_price,unit) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sv.getName());
            ps.setDouble(2, sv.getUnitPrice());
            ps.setString(3, sv.getUnit());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Service sv) throws SQLException {
        String sql = "UPDATE services SET name=?,unit_price=?,unit=?,active=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sv.getName());
            ps.setDouble(2, sv.getUnitPrice());
            ps.setString(3, sv.getUnit());
            ps.setBoolean(4, sv.isActive());
            ps.setInt(5, sv.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE services SET active=0 WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Service findById(int id) throws SQLException {
        String sql = "SELECT * FROM services WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public boolean existsName(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM services WHERE name=? AND active=1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean existsNameExcludeId(String name, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM services WHERE name=? AND active=1 AND id<>?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public List<Service> findAll() throws SQLException {
        String sql = "SELECT * FROM services WHERE active=1 ORDER BY name";
        List<Service> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
}
