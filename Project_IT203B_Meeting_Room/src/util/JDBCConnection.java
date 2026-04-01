package util;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnection {
    private static JDBCConnection instance;

    private static final String URL ="jdbc:mysql://localhost:3306/meeting_room_db";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private Connection connection;

    private JDBCConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy MySQL JDBC Driver: " + e.getMessage());
        }
    }

    public static JDBCConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (JDBCConnection.class) {
                if (instance == null) {
                    instance = new JDBCConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception e) {
                throw new SQLException("Không thể kết nối lại CSDL: " + e.getMessage());
            }
        }
        return connection;
    }

    public static Connection connect() throws SQLException {
        return getInstance().getConnection();
    }

    public static void closeConnection() {
        if (instance != null) {
            try {
                if (instance.connection != null && !instance.connection.isClosed()) {
                    instance.connection.close();
                    System.out.println("Đã đóng kết nối CSDL.");
                }
            } catch (SQLException ignored) {}
        }
    }

}
