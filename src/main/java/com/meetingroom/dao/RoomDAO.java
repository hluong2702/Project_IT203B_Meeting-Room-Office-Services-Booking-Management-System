package com.meetingroom.dao;

import com.meetingroom.model.Room;
import com.meetingroom.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO thao tác với bảng rooms. */
public class RoomDAO {

    private Room map(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        r.setCapacity(rs.getInt("capacity"));
        r.setLocation(rs.getString("location"));
        r.setFixedEquipment(rs.getString("fixed_equipment"));
        r.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }

    public boolean insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (name,capacity,location,fixed_equipment) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, room.getName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getLocation());
            ps.setString(4, room.getFixedEquipment());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET name=?,capacity=?,location=?,fixed_equipment=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, room.getName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getLocation());
            ps.setString(4, room.getFixedEquipment());
            ps.setInt(5, room.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE rooms SET active=0 WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Room findById(int id) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE id=? AND active=1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public boolean existsName(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE name=? AND active=1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean existsNameExcludeId(String name, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE name=? AND active=1 AND id<>?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT * FROM rooms WHERE active=1 ORDER BY name";
        List<Room> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Room> searchByName(String keyword) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE active=1 AND name LIKE ? ORDER BY name";
        List<Room> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Kiểm tra phòng có booking chưa bị hủy không (dùng trước khi xóa). */
    public boolean hasActiveBooking(int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id=? AND status NOT IN ('CANCELLED','REJECTED')";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
