package dao.impl;

import model.Role;
import model.User;
import util.JDBCConnection;
import util.PasswordHash;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersDao {

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setDepartment(rs.getString("department"));
        u.setRole(Role.from(rs.getString("role")));
        u.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }

    // Thêm
    public boolean insert(User u) throws SQLException {
        String sql = "INSERT INTO users (username,password,full_name,email,phone,department,role) VALUES (?,?,?,?,?,?,?)";
        try (Connection c =JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getDepartment());
            ps.setString(7, u.getRole().name());
            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật
    public boolean update(User u) throws SQLException {
        String sql = "UPDATE users SET full_name=?,email=?,phone=?,department=? WHERE id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getDepartment());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật mật khẩu
    public boolean updatePassword(int userId, String hashedPwd) throws SQLException {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hashedPwd);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // Tìm theo username
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=? AND active=1";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    // Tìm theo id
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    // Kiểm tra trùng user
    public boolean existsUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Danh sách tất cả
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users WHERE active=1 ORDER BY role, full_name";
        List<User> list = new ArrayList<>();
        try (Connection c = JDBCConnection.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // Danh sách theo role
    public List<User> findByRole(Role role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role=? AND active=1 ORDER BY full_name";
        List<User> list = new ArrayList<>();
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

}
