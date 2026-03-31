package com.meetingroom.presentation;

import com.meetingroom.model.Booking;
import com.meetingroom.util.ConsoleUtil;

import java.util.List;

/**
 * Helper tĩnh: in danh sách và chi tiết booking lên console.
 * Dùng chung cho cả 3 vai trò.
 */
public class BookingHelper {

    /** In danh sách booking dạng bảng rút gọn. */
    public static void printBookingList(List<Booking> list) {
        if (list.isEmpty()) {
            ConsoleUtil.info("Không có booking nào.");
            return;
        }
        String[] headers = {"ID", "Tiêu đề", "Phòng", "Bắt đầu", "Kết thúc", "Trạng thái", "Chuẩn bị"};
        int[]    widths  = { 4,    22,         16,      16,         16,          11,            16        };

        String[][] rows = new String[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            Booking b = list.get(i);
            rows[i] = new String[]{
                String.valueOf(b.getId()),
                b.getTitle(),
                b.getRoomName() != null ? b.getRoomName() : "-",
                b.getStartTime().format(ConsoleUtil.DATE_FMT),
                b.getEndTime().format(ConsoleUtil.DATE_FMT),
                translateStatus(b.getStatus()),
                translatePrep(b.getPreparationStatus())
            };
        }
        ConsoleUtil.printTable(headers, widths, rows);
    }

    /** In chi tiết đầy đủ một booking. */
    public static void printBookingDetail(Booking b) {
        ConsoleUtil.printSeparator();
        System.out.printf("  Booking ID  : %d%n", b.getId());
        System.out.printf("  Tiêu đề     : %s%n", b.getTitle());
        System.out.printf("  Người đặt   : %s%n", b.getUserFullName() != null ? b.getUserFullName() : "-");
        System.out.printf("  Phòng họp   : %s%n", b.getRoomName() != null ? b.getRoomName() : "-");
        System.out.printf("  Bắt đầu     : %s%n", b.getStartTime().format(ConsoleUtil.DATE_FMT));
        System.out.printf("  Kết thúc    : %s%n", b.getEndTime().format(ConsoleUtil.DATE_FMT));
        System.out.printf("  Số người    : %d%n", b.getAttendeesCount());
        System.out.printf("  Trạng thái  : %s%n", translateStatus(b.getStatus()));
        System.out.printf("  Chuẩn bị    : %s%n", translatePrep(b.getPreparationStatus()));
        if (b.getAssignedStaffName() != null)
            System.out.printf("  Nhân viên HT: %s%n", b.getAssignedStaffName());
        if (b.getRejectReason() != null && !b.getRejectReason().isBlank())
            System.out.printf("  Lý do từ chối: %s%n", b.getRejectReason());
        if (b.getNote() != null && !b.getNote().isBlank())
            System.out.printf("  Ghi chú     : %s%n", b.getNote());

        // Thiết bị đặt kèm
        if (!b.getEquipmentList().isEmpty()) {
            System.out.println("  Thiết bị mượn:");
            for (Booking.BookingEquipment be : b.getEquipmentList())
                System.out.printf("    - %s x%d%n", be.equipmentName, be.quantity);
        }

        // Dịch vụ đặt kèm
        if (!b.getServiceList().isEmpty()) {
            System.out.println("  Dịch vụ đi kèm:");
            double total = 0;
            for (Booking.BookingService bs : b.getServiceList()) {
                double sub = bs.unitPrice * bs.quantity;
                System.out.printf("    - %s x%d = %,.0f đ%n", bs.serviceName, bs.quantity, sub);
                total += sub;
            }
            System.out.printf("  Tổng chi phí dịch vụ: %,.0f đ%n", total);
        }
        ConsoleUtil.printSeparator();
    }

    // ──── Dịch enum sang tiếng Việt ────
    public static String translateStatus(Booking.Status s) {
        return switch (s) {
            case PENDING   -> "Chờ duyệt";
            case APPROVED  -> "Đã duyệt";
            case REJECTED  -> "Từ chối";
            case CANCELLED -> "Đã hủy";
        };
    }

    public static String translatePrep(Booking.PrepStatus p) {
        return switch (p) {
            case NOT_STARTED       -> "Chưa bắt đầu";
            case PREPARING         -> "Đang chuẩn bị";
            case READY             -> "Sẵn sàng";
            case MISSING_EQUIPMENT -> "Thiếu thiết bị";
        };
    }
}
