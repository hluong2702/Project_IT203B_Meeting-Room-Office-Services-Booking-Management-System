package dao.impl;

import model.Booking;
import util.JDBCConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {
    private Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setUserFullName(rs.getString("user_full_name"));
        b.setRoomId(rs.getInt("room_id"));
        b.setRoomName(rs.getString("room_name"));
        b.setTitle(rs.getString("title"));
        b.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        b.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        b.setAttendeesCount(rs.getInt("attendees_count"));
        b.setStatus(Booking.Status.valueOf(rs.getString("status")));
        b.setPreparationStatus(Booking.PrepStatus.valueOf(rs.getString("preparation_status")));
        int staffId = rs.getInt("assigned_staff_id");
        if (!rs.wasNull()) b.setAssignedStaffId(staffId);
        b.setAssignedStaffName(rs.getString("staff_full_name"));
        b.setRejectReason(rs.getString("reject_reason"));
        b.setNote(rs.getString("note"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        return b;
    }

    // SQL lấy booking kèm tên phòng + tên người dùng
    private static final String BASE_SQL =
            "SELECT b.*, u.full_name AS user_full_name, r.name AS room_name, " +
                    "       s.full_name AS staff_full_name " +
                    "FROM bookings b " +
                    "JOIN users u ON b.user_id = u.id " +
                    "JOIN rooms r ON b.room_id = r.id " +
                    "LEFT JOIN users s ON b.assigned_staff_id = s.id ";

    // Thêm booking
    public int insert(Booking b) throws SQLException {
        String sql = "INSERT INTO bookings (user_id,room_id,title,start_time,end_time," +
                "attendees_count,note) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getRoomId());
            ps.setString(3, b.getTitle());
            ps.setTimestamp(4, Timestamp.valueOf(b.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(b.getEndTime()));
            ps.setInt(6, b.getAttendeesCount());
            ps.setString(7, b.getNote());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    // Thêm thiết bị đi kèm
    public void insertEquipment(int bookingId, int equipmentId, int quantity) throws SQLException {
        String sql = "INSERT INTO booking_equipment (booking_id,equipment_id,quantity) VALUES (?,?,?)";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, equipmentId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    // Thêm dịch vụ đi kèm
    public void insertService(int bookingId, int serviceId, int quantity) throws SQLException {
        String sql = "INSERT INTO booking_services (booking_id,service_id,quantity) VALUES (?,?,?)";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    // Cập nhật trạng thái duyệt
    public boolean updateStatus(int id, Booking.Status status, String rejectReason) throws SQLException {
        String sql = "UPDATE bookings SET status=?, reject_reason=? WHERE id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, rejectReason);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật trạng thái chuẩn bị
    public boolean updatePrepStatus(int id, Booking.PrepStatus prepStatus) throws SQLException {
        String sql = "UPDATE bookings SET preparation_status=? WHERE id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, prepStatus.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Phân công nhân viên hỗ trợ
    public boolean assignStaff(int bookingId, int staffId) throws SQLException {
        String sql = "UPDATE bookings SET assigned_staff_id=? WHERE id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    // Tìm theo id
    public Booking findById(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE b.id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Booking b = map(rs);
            loadEquipmentAndServices(b);
            return b;
        }
    }

    // Danh sách booking của một nhân viên
    public List<Booking> findByUser(int userId) throws SQLException {
        String sql = BASE_SQL + "WHERE b.user_id=? ORDER BY b.start_time DESC";
        return queryList(sql, userId);
    }

    // Danh sách booking pemding
    public List<Booking> findPending() throws SQLException {
        String sql = BASE_SQL + "WHERE b.status='PENDING' ORDER BY b.created_at";
        return queryListNoParam(sql);
    }

    // Danh sách được phân công cho STAFF
    public List<Booking> findByAssignedStaff(int staffId) throws SQLException {
        String sql = BASE_SQL + "WHERE b.assigned_staff_id=? AND b.status='APPROVED' " +
                "ORDER BY b.start_time";
        return queryList(sql, staffId);
    }

    // Danh sách tất cả Admin
    public List<Booking> findAll() throws SQLException {
        String sql = BASE_SQL + "ORDER BY b.start_time DESC";
        return queryListNoParam(sql);
    }

    public boolean hasConflict(int roomId, LocalDateTime start, LocalDateTime end,
                               int excludeBookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE room_id=? AND id<>? AND status IN ('APPROVED','PENDING') " +
                "AND start_time < ? AND end_time > ?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, excludeBookingId < 0 ? -1 : excludeBookingId);
            ps.setTimestamp(3, Timestamp.valueOf(end));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Tải thiết bị dịch vụ đi kèm cho booking
    public void loadEquipmentAndServices(Booking b) throws SQLException {
        // Thiết bị
        String sqlEq = "SELECT be.quantity, e.id AS eid, e.name AS ename " +
                "FROM booking_equipment be JOIN equipment e ON be.equipment_id=e.id " +
                "WHERE be.booking_id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sqlEq)) {
            ps.setInt(1, b.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                b.getEquipmentList().add(
                        new Booking.BookingEquipment(rs.getInt("eid"), rs.getString("ename"), rs.getInt("quantity")));
            }
        }
        // Dịch vụ
        String sqlSv = "SELECT bs.quantity, sv.id AS sid, sv.name AS sname, sv.unit_price " +
                "FROM booking_services bs JOIN services sv ON bs.service_id=sv.id " +
                "WHERE bs.booking_id=?";
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sqlSv)) {
            ps.setInt(1, b.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                b.getServiceList().add(
                        new Booking.BookingService(rs.getInt("sid"), rs.getString("sname"),
                                rs.getInt("quantity"), rs.getDouble("unit_price")));
            }
        }
    }

    // HELPER
    private List<Booking> queryList(String sql, int param) throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection c = JDBCConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private List<Booking> queryListNoParam(String sql) throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection c = JDBCConnection.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
}
