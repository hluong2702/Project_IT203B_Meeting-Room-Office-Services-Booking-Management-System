package com.meetingroom.presentation;

import com.meetingroom.model.Booking;
import com.meetingroom.model.User;
import com.meetingroom.service.BookingService;
import com.meetingroom.util.ConsoleUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Menu dành cho vai trò SUPPORT_STAFF:
 *  1. Xem danh sách booking được phân công
 *  2. Cập nhật trạng thái chuẩn bị
 *  3. Xem chi tiết booking
 */
public class SupportStaffMenu {

    private final User           currentUser;
    private final BookingService bookingService = new BookingService();

    public SupportStaffMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("MENU NHÂN VIÊN HỖ TRỢ – " + currentUser.getFullName());
            System.out.println("  1. Danh sách cuộc họp được phân công");
            System.out.println("  2. Cập nhật trạng thái chuẩn bị");
            System.out.println("  3. Xem chi tiết cuộc họp");
            System.out.println("  0. Đăng xuất");
            ConsoleUtil.printSeparator();
            int choice = ConsoleUtil.readChoice("Chọn: ");

            try {
                switch (choice) {
                    case 1 -> viewAssignedBookings();
                    case 2 -> updatePrepStatus();
                    case 3 -> viewDetail();
                    case 0 -> { ConsoleUtil.info("Đã đăng xuất."); return; }
                    default -> ConsoleUtil.error("Lựa chọn không hợp lệ.");
                }
            } catch (SQLException e) {
                ConsoleUtil.error("Lỗi hệ thống: " + e.getMessage());
            }
        }
    }

    // ──── 1. DANH SÁCH ĐƯỢC PHÂN CÔNG ────
    private void viewAssignedBookings() throws SQLException {
        ConsoleUtil.printHeader("CUỘC HỌP ĐƯỢC PHÂN CÔNG");
        List<Booking> list = bookingService.getAssignedBookings(currentUser.getId());
        BookingHelper.printBookingList(list);
        ConsoleUtil.pause();
    }

    // ──── 2. CẬP NHẬT TRẠNG THÁI CHUẨN BỊ ────
    private void updatePrepStatus() throws SQLException {
        ConsoleUtil.printHeader("CẬP NHẬT TRẠNG THÁI CHUẨN BỊ");
        List<Booking> list = bookingService.getAssignedBookings(currentUser.getId());
        BookingHelper.printBookingList(list);
        if (list.isEmpty()) { ConsoleUtil.pause(); return; }

        int id = ConsoleUtil.inputInt("Nhập ID booking cần cập nhật: ");
        Booking b = bookingService.findById(id);
        if (b == null) {
            ConsoleUtil.error("Không tìm thấy booking."); ConsoleUtil.pause(); return;
        }
        if (b.getAssignedStaffId() == null || b.getAssignedStaffId() != currentUser.getId()) {
            ConsoleUtil.error("Bạn không được phân công cho booking này."); ConsoleUtil.pause(); return;
        }

        System.out.println("\nChọn trạng thái chuẩn bị mới:");
        System.out.println("  1. Đang chuẩn bị (PREPARING)");
        System.out.println("  2. Sẵn sàng (READY)");
        System.out.println("  3. Thiếu thiết bị (MISSING_EQUIPMENT)");
        int sel = ConsoleUtil.readChoice("Chọn: ");

        Booking.PrepStatus ps = switch (sel) {
            case 1 -> Booking.PrepStatus.PREPARING;
            case 2 -> Booking.PrepStatus.READY;
            case 3 -> Booking.PrepStatus.MISSING_EQUIPMENT;
            default -> null;
        };
        if (ps == null) { ConsoleUtil.error("Lựa chọn không hợp lệ."); ConsoleUtil.pause(); return; }

        String err = bookingService.updatePrepStatus(id, currentUser.getId(), ps);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cập nhật trạng thái thành công: " + BookingHelper.translatePrep(ps));
        ConsoleUtil.pause();
    }

    // ──── 3. XEM CHI TIẾT ────
    private void viewDetail() throws SQLException {
        ConsoleUtil.printHeader("CHI TIẾT CUỘC HỌP");
        int id = ConsoleUtil.inputInt("Nhập ID booking: ");
        Booking b = bookingService.findById(id);
        if (b == null || b.getAssignedStaffId() == null
                || b.getAssignedStaffId() != currentUser.getId()) {
            ConsoleUtil.error("Không tìm thấy hoặc bạn không có quyền xem booking này.");
            ConsoleUtil.pause(); return;
        }
        BookingHelper.printBookingDetail(b);
        ConsoleUtil.pause();
    }
}
