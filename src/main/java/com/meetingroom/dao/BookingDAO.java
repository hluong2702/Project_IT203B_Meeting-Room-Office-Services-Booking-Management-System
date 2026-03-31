package com.meetingroom.dao;

import com.meetingroom.model.Booking;
import com.meetingroom.model.Booking.BookingEquipment;
import com.meetingroom.model.Booking.BookingService;
import com.meetingroom.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO thao tác với bảng bookings, booking_equipment, booking_services. */
public class BookingDAO {

    // ──── Map ResultSet → Booking ────
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

    // ──── SQL lấy booking kèm tên phòng + tên người dùng ────
    private static final String BASE_SQL =
        "SELECT b.*, u.full_name AS user_full_name, r.name AS room_name, " +
        "       s.full_name AS staff_full_name " +
        "FROM bookings b " +
        "JOIN users u ON b.user_id = u.id " +
        "JOIN rooms r ON b.room_id = r.id " +
        "LEFT JOIN users s ON b.assigned_staff_id = s.id ";

    // ──── THÊM BOOKING ────
    public int insert(Booking b) throws SQLException {
        String sql = "INSERT INTO bookings (user_id,room_id,title,start_time,end_time," +
                     "attendees_count,note) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
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

    // ──── THÊM THIẾT BỊ ĐI KÈM ────
    public void insertEquipment(int bookingId, int equipmentId, int quantity) throws SQLException {
        String sql = "INSERT INTO booking_equipment (booking_id,equipment_id,quantity) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, equipmentId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    // ──── THÊM DỊCH VỤ ĐI KÈM ────
    public void insertService(int bookingId, int serviceId, int quantity) throws SQLException {
        String sql = "INSERT INTO booking_services (booking_id,service_id,quantity) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    // ──── CẬP NHẬT TRẠNG THÁI DUYỆT ────
    public boolean updateStatus(int id, Booking.Status status, String rejectReason) throws SQLException {
        String sql = "UPDATE bookings SET status=?, reject_reason=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, rejectReason);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ──── CẬP NHẬT TRẠNG THÁI CHUẨN BỊ ────
    public boolean updatePrepStatus(int id, Booking.PrepStatus prepStatus) throws SQLException {
        String sql = "UPDATE bookings SET preparation_status=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, prepStatus.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ──── PHÂN CÔNG NHÂN VIÊN HỖ TRỢ ────
    public boolean assignStaff(int bookingId, int staffId) throws SQLException {
        String sql = "UPDATE bookings SET assigned_staff_id=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    // ──── TÌM THEO ID ────
    public Booking findById(int id) throws SQLException {
        String sql = BASE_SQL + "WHERE b.id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Booking b = map(rs);
            loadEquipmentAndServices(b);
            return b;
        }
    }

    // ──── DANH SÁCH BOOKING CỦA MỘT NHÂN VIÊN ────
    public List<Booking> findByUser(int userId) throws SQLException {
        String sql = BASE_SQL + "WHERE b.user_id=? ORDER BY b.start_time DESC";
        return queryList(sql, userId);
    }

    // ──── DANH SÁCH BOOKING PENDING ────
    public List<Booking> findPending() throws SQLException {
        String sql = BASE_SQL + "WHERE b.status='PENDING' ORDER BY b.created_at";
        return queryListNoParam(sql);
    }

    // ──── DANH SÁCH ĐƯỢC PHÂN CÔNG CHO STAFF ────
    public List<Booking> findByAssignedStaff(int staffId) throws SQLException {
        String sql = BASE_SQL + "WHERE b.assigned_staff_id=? AND b.status='APPROVED' " +
                     "ORDER BY b.start_time";
        return queryList(sql, staffId);
    }

    // ──── DANH SÁCH TẤT CẢ (ADMIN) ────
    public List<Booking> findAll() throws SQLException {
        String sql = BASE_SQL + "ORDER BY b.start_time DESC";
        return queryListNoParam(sql);
    }

    // ──── KIỂM TRA XUNG ĐỘT LỊCH PHÒNG ────
    /**
     * Kiểm tra phòng roomId có booking nào (APPROVED/PENDING) bị trùng
     * khoảng [start, end) không. excludeBookingId = -1 nếu không cần loại trừ.
     */
    public boolean hasConflict(int roomId, LocalDateTime start, LocalDateTime end,
                                int excludeBookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings " +
                     "WHERE room_id=? AND id<>? AND status IN ('APPROVED','PENDING') " +
                     "AND start_time < ? AND end_time > ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, excludeBookingId < 0 ? -1 : excludeBookingId);
            ps.setTimestamp(3, Timestamp.valueOf(end));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // ──── TẢI THIẾT BỊ VÀ DỊCH VỤ KÈM ────
    public void loadEquipmentAndServices(Booking b) throws SQLException {
        // Thiết bị
        String sqlEq = "SELECT be.quantity, e.id AS eid, e.name AS ename " +
                       "FROM booking_equipment be JOIN equipment e ON be.equipment_id=e.id " +
                       "WHERE be.booking_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlEq)) {
            ps.setInt(1, b.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                b.getEquipmentList().add(
                    new BookingEquipment(rs.getInt("eid"), rs.getString("ename"), rs.getInt("quantity")));
            }
        }
        // Dịch vụ
        String sqlSv = "SELECT bs.quantity, sv.id AS sid, sv.name AS sname, sv.unit_price " +
                       "FROM booking_services bs JOIN services sv ON bs.service_id=sv.id " +
                       "WHERE bs.booking_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlSv)) {
            ps.setInt(1, b.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                b.getServiceList().add(
                    new BookingService(rs.getInt("sid"), rs.getString("sname"),
                                       rs.getInt("quantity"), rs.getDouble("unit_price")));
            }
        }
    }

    // ──── HELPER ────
    private List<Booking> queryList(String sql, int param) throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private List<Booking> queryListNoParam(String sql) throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
}
