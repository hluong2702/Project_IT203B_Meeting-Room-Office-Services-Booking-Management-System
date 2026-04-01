package service;

import dao.impl.BookingDao;
import dao.impl.EquipmentDao;
import dao.impl.RoomDao;
import model.Booking;
import model.Equipment;
import model.Room;
import util.Validate;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BookingService {

    private final BookingDao bookingDAO    = new BookingDao();
    private final RoomDao roomDAO       = new RoomDao();
    private final EquipmentDao equipmentDAO  = new EquipmentDao();

    public String createBooking(int userId, int roomId, String title,
                                LocalDateTime start, LocalDateTime end, int attendees,
                                String note,
                                Map<Integer, Integer> equipmentMap,
                                Map<Integer, Integer> serviceMap) throws SQLException {

        // 1. Kiểm tra dữ liệu cơ bản
        if (title == null || title.isBlank())    return "Tiêu đề cuộc họp không được để trống.";
        if (!Validate.isFutureTime(start))      return "Thời gian bắt đầu phải ở tương lai.";
        if (!Validate.isEndAfterStart(start, end)) return "Thời gian kết thúc phải sau thời gian bắt đầu.";
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

    //  Huỷ Booking (Employee – chỉ hủy được khi PENDING)
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

    //  Duyệt từ chối booking (Admin)
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

    //  Phân công nhân viên hỗ trợ (Admin)
    public String assignStaff(int bookingId, int staffId) throws SQLException {
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) return "Không tìm thấy booking ID = " + bookingId;
        if (b.getStatus() != Booking.Status.APPROVED)
            return "Chỉ phân công cho booking đã được APPROVED.";

        bookingDAO.assignStaff(bookingId, staffId);
        return null;
    }

    //  Cập nhật trạng thái chuẩn bị (Support Staff)
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

    //  Truy vấn
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

    //  PRIVATE HELPER – hoàn trả thiết bị
    private void restoreEquipment(Booking b) throws SQLException {
        bookingDAO.loadEquipmentAndServices(b);
        for (Booking.BookingEquipment be : b.getEquipmentList()) {
            equipmentDAO.adjustAvailable(be.equipmentId, be.quantity);
        }
    }

}
