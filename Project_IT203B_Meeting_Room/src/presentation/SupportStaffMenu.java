package presentation;

import model.Booking;
import model.User;
import service.BookingService;
import util.ConsoleUtil;

import java.sql.SQLException;
import java.util.List;

public class SupportStaffMenu {
    private final User currentUser;
    private final BookingService bookingService = new BookingService();

    public SupportStaffMenu(User currentUser) { this.currentUser = currentUser; }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("MENU NHAN VIEN HO TRO - " + currentUser.getFullName());
            System.out.println("1. Danh sach cuoc hop duoc phan cong");
            System.out.println("2. Cap nhat trang thai chuan bi");
            System.out.println("3. Xem chi tiet cuoc hop");
            System.out.println("0. Dang xuat");
            int choice = ConsoleUtil.readChoice("Chon: ");
            try {
                switch (choice) {
                    case 1 -> viewAssignedBookings();
                    case 2 -> updatePrepStatus();
                    case 3 -> viewDetail();
                    case 0 -> { ConsoleUtil.info("Da dang xuat."); return; }
                    default -> ConsoleUtil.error("Lua chon khong hop le.");
                }
            } catch (SQLException e) {
                ConsoleUtil.error("Loi he thong: " + e.getMessage());
            }
        }
    }

    private void viewAssignedBookings() throws SQLException {
        ConsoleUtil.printHeader("CUOC HOP DUOC PHAN CONG");
        BookingHelper.printBookingList(bookingService.getAssignedBookings(currentUser.getId()));
        ConsoleUtil.pause();
    }

    private void updatePrepStatus() throws SQLException {
        ConsoleUtil.printHeader("CAP NHAT TRANG THAI CHUAN BI");
        List<Booking> list = bookingService.getAssignedBookings(currentUser.getId());
        BookingHelper.printBookingList(list);
        if (list.isEmpty()) { ConsoleUtil.pause(); return; }

        int id = ConsoleUtil.inputInt("Nhap ID booking can cap nhat: ");
        Booking b = bookingService.findById(id);
        if (b == null || b.getAssignedStaffId() == null || b.getAssignedStaffId() != currentUser.getId()) {
            ConsoleUtil.error("Khong tim thay hoac ban khong duoc phan cong cho booking nay.");
            ConsoleUtil.pause(); return;
        }

        System.out.println("Chon trang thai chuan bi:");
        System.out.println("1. Dang chuan bi (PREPARING)");
        System.out.println("2. San sang (READY)");
        System.out.println("3. Thieu thiet bi (MISSING_EQUIPMENT)");
        int sel = ConsoleUtil.readChoice("Chon: ");

        Booking.PrepStatus ps = switch (sel) {
            case 1 -> Booking.PrepStatus.PREPARING;
            case 2 -> Booking.PrepStatus.READY;
            case 3 -> Booking.PrepStatus.MISSING_EQUIPMENT;
            default -> null;
        };
        if (ps == null) { ConsoleUtil.error("Lua chon khong hop le."); ConsoleUtil.pause(); return; }

        String err = bookingService.updatePrepStatus(id, currentUser.getId(), ps);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cap nhat trang thai: " + BookingHelper.translatePrep(ps));
        ConsoleUtil.pause();
    }

    private void viewDetail() throws SQLException {
        ConsoleUtil.printHeader("CHI TIET CUOC HOP");
        int id = ConsoleUtil.inputInt("Nhap ID booking: ");
        Booking b = bookingService.findById(id);
        if (b == null || b.getAssignedStaffId() == null || b.getAssignedStaffId() != currentUser.getId()) {
            ConsoleUtil.error("Khong tim thay hoac ban khong co quyen xem booking nay.");
            ConsoleUtil.pause(); return;
        }
        BookingHelper.printBookingDetail(b);
        ConsoleUtil.pause();
    }
}
