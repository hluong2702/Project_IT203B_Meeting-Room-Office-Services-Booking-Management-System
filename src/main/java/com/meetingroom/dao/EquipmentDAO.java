package com.meetingroom.dao;

import com.meetingroom.model.Equipment;
import com.meetingroom.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO thao tác với bảng equipment. */
public class EquipmentDAO {

    private Equipment map(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setTotalQuantity(rs.getInt("total_quantity"));
        e.setAvailableQty(rs.getInt("available_qty"));
        e.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
        return e;
    }

    public boolean insert(Equipment eq) throws SQLException {
        String sql = "INSERT INTO equipment (name,total_quantity,available_qty,status) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, eq.getName());
            ps.setInt(2, eq.getTotalQuantity());
            ps.setInt(3, eq.getAvailableQty());
            ps.setString(4, eq.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Equipment eq) throws SQLException {
        String sql = "UPDATE equipment SET name=?,total_quantity=?,available_qty=?,status=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, eq.getName());
            ps.setInt(2, eq.getTotalQuantity());
            ps.setInt(3, eq.getAvailableQty());
            ps.setString(4, eq.getStatus());
            ps.setInt(5, eq.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM equipment WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Equipment findById(int id) throws SQLException {
        String sql = "SELECT * FROM equipment WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public boolean existsName(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM equipment WHERE name=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean existsNameExcludeId(String name, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM equipment WHERE name=? AND id<>?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public List<Equipment> findAll() throws SQLException {
        String sql = "SELECT * FROM equipment ORDER BY name";
        List<Equipment> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Equipment> findActive() throws SQLException {
        String sql = "SELECT * FROM equipment WHERE status='ACTIVE' AND available_qty>0 ORDER BY name";
        List<Equipment> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Trừ / cộng lại số lượng khả dụng khi đặt / hủy booking. */
    public boolean adjustAvailable(int id, int delta) throws SQLException {
        String sql = "UPDATE equipment SET available_qty = available_qty + ? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isUsedInBooking(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM booking_equipment WHERE equipment_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
