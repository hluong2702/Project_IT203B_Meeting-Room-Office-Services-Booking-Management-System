package com.meetingroom.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối CSDL MySQL.
 * Dùng Singleton pattern để tái sử dụng kết nối.
 */
public class DBConnection {

    // ──────────────── CẤU HÌNH KẾT NỐI ────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/meeting_room_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    private static Connection connection;

    /** Trả về Connection (tạo mới nếu chưa có hoặc đã đóng). */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy MySQL JDBC Driver: " + e.getMessage());
            }
        }
        return connection;
    }

    /** Đóng kết nối khi thoát chương trình. */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }
}
