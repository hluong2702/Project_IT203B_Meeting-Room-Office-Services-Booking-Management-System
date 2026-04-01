package presentation;

import model.Booking;
import util.ConsoleUtil;

import java.util.List;

public class BookingHelper {
    public static void printBookingList(List<Booking> list) {
        if (list.isEmpty()) {
            ConsoleUtil.info("Khong co booking nao.");
            return;
        }
        ConsoleUtil.printSeparator();
        for (Booking b : list) {
            System.out.printf("ID: %-4d | %s | Phong: %s | %s -> %s | %s | %s%n",
                    b.getId(),
                    b.getTitle(),
                    b.getRoomName() != null ? b.getRoomName() : "-",
                    b.getStartTime().format(ConsoleUtil.DATE_FMT),
                    b.getEndTime().format(ConsoleUtil.DATE_FMT),
                    translateStatus(b.getStatus()),
                    translatePrep(b.getPreparationStatus())
            );
        }
        ConsoleUtil.printSeparator();
    }

    // In chi tiết đầy đủ một booking.
    public static void printBookingDetail(Booking b) {
        ConsoleUtil.printSeparator();
        System.out.println("Booking ID  : " + b.getId());
        System.out.println("Tieu de     : " + b.getTitle());
        System.out.println("Nguoi dat   : " + (b.getUserFullName() != null ? b.getUserFullName() : "-"));
        System.out.println("Phong hop   : " + (b.getRoomName() != null ? b.getRoomName() : "-"));
        System.out.println("Bat dau     : " + b.getStartTime().format(ConsoleUtil.DATE_FMT));
        System.out.println("Ket thuc    : " + b.getEndTime().format(ConsoleUtil.DATE_FMT));
        System.out.println("So nguoi    : " + b.getAttendeesCount());
        System.out.println("Trang thai  : " + translateStatus(b.getStatus()));
        System.out.println("Chuan bi    : " + translatePrep(b.getPreparationStatus()));

        if (b.getAssignedStaffName() != null)
            System.out.println("NV ho tro   : " + b.getAssignedStaffName());
        if (b.getRejectReason() != null && !b.getRejectReason().isBlank())
            System.out.println("Ly do tu choi: " + b.getRejectReason());
        if (b.getNote() != null && !b.getNote().isBlank())
            System.out.println("Ghi chu     : " + b.getNote());

        if (!b.getEquipmentList().isEmpty()) {
            System.out.println("Thiet bi muon:");
            for (Booking.BookingEquipment be : b.getEquipmentList())
                System.out.println("  - " + be.equipmentName + " x" + be.quantity);
        }

        if (!b.getServiceList().isEmpty()) {
            System.out.println("Dich vu di kem:");
            double total = 0;
            for (Booking.BookingService bs : b.getServiceList()) {
                double sub = bs.unitPrice * bs.quantity;
                System.out.printf("  - %s x%d = %,.0f d%n", bs.serviceName, bs.quantity, sub);
                total += sub;
            }
            System.out.printf("Tong chi phi dich vu: %,.0f d%n", total);
        }
        ConsoleUtil.printSeparator();
    }

    public static String translateStatus(Booking.Status s) {
        return switch (s) {
            case PENDING   -> "Cho duyet";
            case APPROVED  -> "Da duyet";
            case REJECTED  -> "Tu choi";
            case CANCELLED -> "Da huy";
        };
    }

    public static String translatePrep(Booking.PrepStatus p) {
        return switch (p) {
            case NOT_STARTED       -> "Chua bat dau";
            case PREPARING         -> "Dang chuan bi";
            case READY             -> "San sang";
            case MISSING_EQUIPMENT -> "Thieu thiet bi";
        };
    }
}
