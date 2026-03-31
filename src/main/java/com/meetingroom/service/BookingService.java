package com.meetingroom.service;

import com.meetingroom.dao.BookingDAO;
import com.meetingroom.dao.EquipmentDAO;
import com.meetingroom.dao.RoomDAO;
import com.meetingroom.model.Booking;
import com.meetingroom.model.Equipment;
import com.meetingroom.model.Room;
import com.meetingroom.util.Validator;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý toàn bộ nghiệp vụ đặt phòng:
 *  - Tạo booking (Employee)
 *  - Duyệt / từ chối (Admin)
 *  - Phân công nhân viên hỗ trợ (Admin)
 *  - Cập nhật trạng thái chuẩn bị (Support Staff)
 *  - Hủy booking (Employee)
 */
public class BookingService {

    private final BookingDAO    bookingDAO    = new BookingDAO();
    private final RoomDAO       roomDAO       = new RoomDAO();
    private final EquipmentDAO  equipmentDAO  = new EquipmentDAO();

    // ──────────────────────────────────────────────────────────────
    //  TẠO BOOKING (Employee)
    // ──────────────────────────────────────────────────────────────
    /**
     * Tạo booking mới.
     * @param userId         ID người đặt
     * @param roomId         ID phòng họp
     * @param title          Tiêu đề cuộc họp
     * @param start          Thời gian bắt đầu
     * @param end            Thời gian kết thúc
     * @param attendees      Số người tham dự
     * @param note           Ghi chú thêm (có thể null)
     * @param equipmentMap   Map<equipmentId, quantity> – thiết bị cần mượn
     * @param serviceMap     Map<serviceId, quantity>   – dịch vụ đi kèm
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String createBooking(int userId, int roomId, String title,
                                LocalDateTime start, LocalDateTime end, int attendees,
                                String note,
                                Map<Integer, Integer> equipmentMap,
                                Map<Integer, Integer> serviceMap) throws SQLException {

        // 1. Kiểm tra dữ liệu cơ bản
        if (title == null || title.isBlank())    return "Tiêu đề cuộc họp không được để trống.";
        if (!Validator.isFutureTime(start))      return "Thời gian bắt đầu phải ở tương lai.";
        if (!Validator.isEndAfterStart(start, end)) return "Thời gian kết thúc phải sau thời gian bắt đầu.";
        if (attendees <= 0)                      return "Số người tham dự phải lớn hơn 0.";

        // 2. Kiểm tra phòng tồn tại
        Room room = roomDAO.findById(roomId);
        if (room == null) return "Phòng họp không tồn tại.";

        // 3. Kiểm tra sức chứa
        if (attendees > room.getCapacity())
            return String.format("Số người (%d) vượt quá sức chứa của phòng (%d).",
                                 attendees, room.getCapacity());

        // 4. Kiểm tra xung đột lịch
        if (bookingDAO.hasConflict(roomId, start, end, -1))
            return "Phòng đã có lịch đặt trong khung giờ này. Vui lòng chọn giờ khác.";

        // 5. Kiểm tra số lượng thiết bị khả dụng
        if (equipmentMap != null) {
            for (Map.Entry<Integer, Integer> entry : equipmentMap.entrySet()) {
                Equipment eq = equipmentDAO.findById(entry.getKey());
                if (eq == null) return "Thiết bị ID=" + entry.getKey() + " không tồn tại.";
                if (eq.getAvailableQty() < entry.getValue())
                    return "Thiết bị '" + eq.getName() + "' chỉ còn " +
                           eq.getAvailableQty() + " cái, không đủ " + entry.getValue() + " cái yêu cầu.";
            }
        }

        // 6. Lưu booking
        Booking b = new Booking();
        b.setUserId(userId);
        b.setRoomId(roomId);
        b.setTitle(title);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setAttendeesCount(attendees);
        b.setNote(note);

        int bookingId = bookingDAO.insert(b);
        if (bookingId < 0) return "Lỗi khi lưu booking vào cơ sở dữ liệu.";

        // 7. Lưu thiết bị kèm
        if (equipmentMap != null) {
            for (Map.Entry<Integer, Integer> entry : equipmentMap.entrySet()) {
                bookingDAO.insertEquipment(bookingId, entry.getKey(), entry.getValue());
                equipmentDAO.adjustAvailable(entry.getKey(), -entry.getValue());
            }
        }

        // 8. Lưu dịch vụ kèm
        if (serviceMap != null) {
            for (Map.Entry<Integer, Integer> entry : serviceMap.entrySet()) {
                bookingDAO.insertService(bookingId, entry.getKey(), entry.getValue());
            }
        }

        return null; // thành công
    }

    // ──────────────────────────────────────────────────────────────
    //  HỦY BOOKING (Employee – chỉ hủy được khi PENDING)
    // ──────────────────────────────────────────────────────────────
    public String cancelBooking(int bookingId, int requestUserId) throws SQLException {
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) return "Không tìm thấy booking ID = " + bookingId;
        if (b.getUserId() != requestUserId)
            return "Bạn không có quyền hủy booking này.";
        if (b.getStatus() != Booking.Status.PENDING)
            return "Chỉ có thể hủy booking ở trạng thái PENDING. Booking này đang: " +
                   b.getStatus();

        bookingDAO.updateStatus(bookingId, Booking.Status.CANCELLED, null);

        // Hoàn trả thiết bị
        restoreEquipment(b);
        return null;
    }

    // ──────────────────────────────────────────────────────────────
    //  DUYỆT / TỪ CHỐI BOOKING (Admin)
    // ──────────────────────────────────────────────────────────────
    public String approveBooking(int bookingId) throws SQLException {
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) return "Không tìm thấy booking ID = " + bookingId;
        if (b.getStatus() != Booking.Status.PENDING)
            return "Booking này không ở trạng thái PENDING.";

        // Kiểm tra lại xung đột lịch (có thể đã có booking khác được duyệt)
        if (bookingDAO.hasConflict(b.getRoomId(), b.getStartTime(), b.getEndTime(), bookingId))
            return "Phòng đã có lịch APPROVED trùng khung giờ này. Vui lòng từ chối booking.";

        bookingDAO.updateStatus(bookingId, Booking.Status.APPROVED, null);
        return null;
    }

    public String rejectBooking(int bookingId, String reason) throws SQLException {
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) return "Không tìm thấy booking ID = " + bookingId;
        if (b.getStatus() != Booking.Status.PENDING)
            return "Booking này không ở trạng thái PENDING.";

        bookingDAO.updateStatus(bookingId, Booking.Status.REJECTED,
                reason == null || reason.isBlank() ? "Admin từ chối" : reason);

        // Hoàn trả thiết bị
        restoreEquipment(b);
        return null;
    }

    // ──────────────────────────────────────────────────────────────
    //  PHÂN CÔNG NHÂN VIÊN HỖ TRỢ (Admin)
    // ──────────────────────────────────────────────────────────────
    public String assignStaff(int bookingId, int staffId) throws SQLException {
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) return "Không tìm thấy booking ID = " + bookingId;
        if (b.getStatus() != Booking.Status.APPROVED)
            return "Chỉ phân công cho booking đã được APPROVED.";

        bookingDAO.assignStaff(bookingId, staffId);
        return null;
    }

    // ──────────────────────────────────────────────────────────────
    //  CẬP NHẬT TRẠNG THÁI CHUẨN BỊ (Support Staff)
    // ──────────────────────────────────────────────────────────────
    public String updatePrepStatus(int bookingId, int staffId,
                                   Booking.PrepStatus prepStatus) throws SQLException {
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) return "Không tìm thấy booking ID = " + bookingId;
        if (b.getAssignedStaffId() == null || b.getAssignedStaffId() != staffId)
            return "Bạn không được phân công cho booking này.";
        if (b.getStatus() != Booking.Status.APPROVED)
            return "Booking chưa được duyệt.";

        bookingDAO.updatePrepStatus(bookingId, prepStatus);
        return null;
    }

    // ──────────────────────────────────────────────────────────────
    //  TRUY VẤN
    // ──────────────────────────────────────────────────────────────
    public Booking findById(int id) throws SQLException { return bookingDAO.findById(id); }

    public List<Booking> getMyBookings(int userId) throws SQLException {
        return bookingDAO.findByUser(userId);
    }

    public List<Booking> getPendingBookings() throws SQLException {
        return bookingDAO.findPending();
    }

    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.findAll();
    }

    public List<Booking> getAssignedBookings(int staffId) throws SQLException {
        return bookingDAO.findByAssignedStaff(staffId);
    }

    // ──────────────────────────────────────────────────────────────
    //  PRIVATE HELPER – hoàn trả thiết bị
    // ──────────────────────────────────────────────────────────────
    private void restoreEquipment(Booking b) throws SQLException {
        bookingDAO.loadEquipmentAndServices(b);
        for (Booking.BookingEquipment be : b.getEquipmentList()) {
            equipmentDAO.adjustAvailable(be.equipmentId, be.quantity);
        }
    }
}
