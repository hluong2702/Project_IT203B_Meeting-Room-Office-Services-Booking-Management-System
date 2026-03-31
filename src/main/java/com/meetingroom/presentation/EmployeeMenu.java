package com.meetingroom.presentation;

import com.meetingroom.model.*;
import com.meetingroom.service.*;
import com.meetingroom.util.ConsoleUtil;
import com.meetingroom.util.Validator;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Menu dành cho vai trò EMPLOYEE:
 *  1. Đặt phòng họp mới
 *  2. Xem lịch họp của tôi
 *  3. Chi tiết & hủy booking
 *  4. Xem / cập nhật hồ sơ cá nhân
 *  5. Đổi mật khẩu
 */
public class EmployeeMenu {

    private final User                 currentUser;
    private final BookingService       bookingService  = new BookingService();
    private final RoomService          roomService     = new RoomService();
    private final EquipmentService     equipmentService = new EquipmentService();
    private final ServiceMgmtService   serviceMgmt     = new ServiceMgmtService();
    private final AuthService          authService     = new AuthService();

    public EmployeeMenu(User currentUser) {
        this.currentUser = currentUser;
    }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("MENU NHÂN VIÊN – " + currentUser.getFullName());
            System.out.println("  1. Đặt phòng họp mới");
            System.out.println("  2. Xem lịch họp của tôi");
            System.out.println("  3. Chi tiết / Hủy booking");
            System.out.println("  4. Hồ sơ cá nhân");
            System.out.println("  5. Đổi mật khẩu");
            System.out.println("  0. Đăng xuất");
            ConsoleUtil.printSeparator();
            int choice = ConsoleUtil.readChoice("Chọn: ");

            try {
                switch (choice) {
                    case 1 -> createBooking();
                    case 2 -> viewMyBookings();
                    case 3 -> cancelBookingMenu();
                    case 4 -> updateProfile();
                    case 5 -> changePassword();
                    case 0 -> { ConsoleUtil.info("Đã đăng xuất."); return; }
                    default -> ConsoleUtil.error("Lựa chọn không hợp lệ.");
                }
            } catch (SQLException e) {
                ConsoleUtil.error("Lỗi hệ thống: " + e.getMessage());
            }
        }
    }

    // ──── 1. ĐẶT PHÒNG ────
    private void createBooking() throws SQLException {
        ConsoleUtil.printHeader("ĐẶT PHÒNG HỌP");

        // Hiển thị danh sách phòng
        List<Room> rooms = roomService.getAllRooms();
        if (rooms.isEmpty()) { ConsoleUtil.warn("Hiện chưa có phòng họp nào."); ConsoleUtil.pause(); return; }

        String[] rHeaders = {"ID", "Tên phòng", "Sức chứa", "Vị trí", "Thiết bị cố định"};
        int[] rWidths = {4, 20, 9, 20, 28};
        String[][] rRows = rooms.stream().map(r -> new String[]{
            String.valueOf(r.getId()), r.getName(), String.valueOf(r.getCapacity()),
            r.getLocation(), r.getFixedEquipment()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(rHeaders, rWidths, rRows);

        int roomId    = ConsoleUtil.inputInt("Nhập ID phòng: ");
        Room room     = roomService.findById(roomId);
        if (room == null) { ConsoleUtil.error("Phòng không tồn tại."); ConsoleUtil.pause(); return; }

        String title  = ConsoleUtil.inputString("Tiêu đề cuộc họp: ");
        LocalDateTime start = ConsoleUtil.inputDateTime("Thời gian bắt đầu");
        if (!Validator.isFutureTime(start)) {
            ConsoleUtil.error("Thời gian bắt đầu phải ở tương lai."); ConsoleUtil.pause(); return;
        }
        LocalDateTime end = ConsoleUtil.inputDateTime("Thời gian kết thúc");
        if (!Validator.isEndAfterStart(start, end)) {
            ConsoleUtil.error("Thời gian kết thúc phải sau bắt đầu."); ConsoleUtil.pause(); return;
        }
        int attendees = ConsoleUtil.inputInt("Số người tham dự: ");
        String note   = ConsoleUtil.inputOptional("Ghi chú (Enter để bỏ qua): ");

        // Chọn thiết bị mượn thêm
        Map<Integer, Integer> equipmentMap = selectEquipment();

        // Chọn dịch vụ đi kèm
        Map<Integer, Integer> serviceMap = selectServices();

        // Xác nhận
        System.out.printf("%nXác nhận đặt phòng '%s' từ %s đến %s cho %d người? %n",
                room.getName(), start.format(ConsoleUtil.DATE_FMT),
                end.format(ConsoleUtil.DATE_FMT), attendees);
        if (!ConsoleUtil.confirm("Xác nhận")) { ConsoleUtil.info("Đã hủy."); ConsoleUtil.pause(); return; }

        String err = bookingService.createBooking(currentUser.getId(), roomId, title,
                start, end, attendees, note, equipmentMap, serviceMap);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đặt phòng thành công! Booking đang chờ Admin duyệt.");
        ConsoleUtil.pause();
    }

    // ── Chọn thiết bị ──
    private Map<Integer, Integer> selectEquipment() throws SQLException {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        List<Equipment> list = equipmentService.getActive();
        if (list.isEmpty()) { ConsoleUtil.info("Không có thiết bị di động khả dụng."); return map; }

        System.out.println("\n--- THIẾT BỊ DI ĐỘNG CÓ THỂ MƯỢN ---");
        String[] h = {"ID","Tên thiết bị","Khả dụng"};
        int[] w = {4,28,9};
        String[][] r = list.stream().map(e -> new String[]{
                String.valueOf(e.getId()), e.getName(), String.valueOf(e.getAvailableQty())
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, r);

        System.out.println("Nhập các thiết bị cần mượn (định dạng: ID,số_lượng mỗi dòng; nhập 0 để kết thúc):");
        while (true) {
            String input = ConsoleUtil.inputOptional("  > ");
            if (input.equals("0") || input.isBlank()) break;
            try {
                String[] parts = input.split(",");
                int eId  = Integer.parseInt(parts[0].trim());
                int qty  = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                Equipment eq = equipmentService.findById(eId);
                if (eq == null) { ConsoleUtil.error("Thiết bị ID=" + eId + " không tồn tại."); continue; }
                if (qty > eq.getAvailableQty()) {
                    ConsoleUtil.error("Chỉ còn " + eq.getAvailableQty() + " cái " + eq.getName()); continue;
                }
                map.put(eId, qty);
                ConsoleUtil.success("Đã thêm: " + eq.getName() + " x" + qty);
            } catch (Exception e) {
                ConsoleUtil.error("Định dạng sai. Ví dụ: 2,1 (ID=2, số lượng=1)");
            }
        }
        return map;
    }

    // ── Chọn dịch vụ ──
    private Map<Integer, Integer> selectServices() throws SQLException {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        List<Service> list = serviceMgmt.getAll();
        if (list.isEmpty()) return map;

        System.out.println("\n--- DỊCH VỤ ĐI KÈM ---");
        String[] h = {"ID","Tên dịch vụ","Đơn giá","Đơn vị"};
        int[] w = {4,24,12,8};
        String[][] r = list.stream().map(s -> new String[]{
                String.valueOf(s.getId()), s.getName(),
                String.format("%,.0f đ", s.getUnitPrice()), s.getUnit()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, r);

        System.out.println("Nhập dịch vụ cần đặt (định dạng: ID,số_lượng; nhập 0 để kết thúc):");
        while (true) {
            String input = ConsoleUtil.inputOptional("  > ");
            if (input.equals("0") || input.isBlank()) break;
            try {
                String[] parts = input.split(",");
                int sId = Integer.parseInt(parts[0].trim());
                int qty = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                Service sv = serviceMgmt.findById(sId);
                if (sv == null) { ConsoleUtil.error("Dịch vụ ID=" + sId + " không tồn tại."); continue; }
                map.put(sId, qty);
                ConsoleUtil.success("Đã thêm: " + sv.getName() + " x" + qty);
            } catch (Exception e) {
                ConsoleUtil.error("Định dạng sai. Ví dụ: 1,2 (ID=1, số lượng=2)");
            }
        }
        return map;
    }

    // ──── 2. XEM LỊCH HỌP ────
    private void viewMyBookings() throws SQLException {
        ConsoleUtil.printHeader("LỊCH HỌP CỦA TÔI");
        List<Booking> list = bookingService.getMyBookings(currentUser.getId());
        BookingHelper.printBookingList(list);
        ConsoleUtil.pause();
    }

    // ──── 3. CHI TIẾT / HỦY ────
    private void cancelBookingMenu() throws SQLException {
        ConsoleUtil.printHeader("CHI TIẾT / HỦY BOOKING");
        List<Booking> list = bookingService.getMyBookings(currentUser.getId());
        BookingHelper.printBookingList(list);
        if (list.isEmpty()) { ConsoleUtil.pause(); return; }

        int id = ConsoleUtil.inputInt("Nhập ID booking để xem chi tiết: ");
        Booking b = bookingService.findById(id);
        if (b == null || b.getUserId() != currentUser.getId()) {
            ConsoleUtil.error("Không tìm thấy booking hoặc không thuộc về bạn.");
            ConsoleUtil.pause(); return;
        }
        BookingHelper.printBookingDetail(b);

        if (b.getStatus() == Booking.Status.PENDING) {
            if (ConsoleUtil.confirm("Bạn có muốn HỦY booking này không?")) {
                String err = bookingService.cancelBooking(id, currentUser.getId());
                if (err != null) ConsoleUtil.error(err);
                else ConsoleUtil.success("Đã hủy booking thành công.");
            }
        } else {
            ConsoleUtil.info("Booking này không ở trạng thái PENDING nên không thể hủy.");
        }
        ConsoleUtil.pause();
    }

    // ──── 4. HỒ SƠ CÁ NHÂN ────
    private void updateProfile() throws SQLException {
        ConsoleUtil.printHeader("HỒ SƠ CÁ NHÂN");
        System.out.printf("  Username   : %s%n", currentUser.getUsername());
        System.out.printf("  Họ tên     : %s%n", currentUser.getFullName());
        System.out.printf("  Email      : %s%n", currentUser.getEmail());
        System.out.printf("  Điện thoại : %s%n", currentUser.getPhone());
        System.out.printf("  Phòng ban  : %s%n", currentUser.getDepartment());
        System.out.printf("  Vai trò    : %s%n", currentUser.getRole().getDisplayName());
        ConsoleUtil.printSeparator();

        if (!ConsoleUtil.confirm("Bạn có muốn cập nhật hồ sơ không?")) { ConsoleUtil.pause(); return; }

        String fullName   = ConsoleUtil.inputString("Họ tên (hiện: " + currentUser.getFullName() + "): ");
        String email      = ConsoleUtil.inputOptional("Email (hiện: " + currentUser.getEmail() + "): ");
        String phone      = ConsoleUtil.inputOptional("SĐT (hiện: " + currentUser.getPhone() + "): ");
        String department = ConsoleUtil.inputOptional("Phòng ban (hiện: " + currentUser.getDepartment() + "): ");

        String err = authService.updateProfile(currentUser,
                fullName.isBlank() ? currentUser.getFullName() : fullName,
                email.isBlank()    ? currentUser.getEmail()    : email,
                phone.isBlank()    ? currentUser.getPhone()    : phone,
                department.isBlank()? currentUser.getDepartment(): department);

        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cập nhật hồ sơ thành công!");
        ConsoleUtil.pause();
    }

    // ──── 5. ĐỔI MẬT KHẨU ────
    private void changePassword() throws SQLException {
        ConsoleUtil.printHeader("ĐỔI MẬT KHẨU");
        String oldPwd = ConsoleUtil.inputPassword("Mật khẩu cũ  : ");
        String newPwd = ConsoleUtil.inputPassword("Mật khẩu mới : ");
        String confirm = ConsoleUtil.inputPassword("Xác nhận lại : ");

        if (!newPwd.equals(confirm)) { ConsoleUtil.error("Mật khẩu xác nhận không khớp."); ConsoleUtil.pause(); return; }

        String err = authService.changePassword(currentUser, oldPwd, newPwd);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đổi mật khẩu thành công!");
        ConsoleUtil.pause();
    }
}
