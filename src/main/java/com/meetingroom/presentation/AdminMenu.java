package com.meetingroom.presentation;

import com.meetingroom.model.*;
import com.meetingroom.service.*;
import com.meetingroom.util.ConsoleUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Menu dành cho vai trò ADMIN – 6 phân hệ:
 *   A. Quản lý Phòng họp
 *   B. Quản lý Thiết bị di động
 *   C. Quản lý Dịch vụ đi kèm
 *   D. Quản lý Người dùng
 *   E. Duyệt & Phân công Booking
 *   F. Xem tất cả booking
 */
public class AdminMenu {

    private final User               currentUser;
    private final RoomService        roomService     = new RoomService();
    private final EquipmentService   equipService    = new EquipmentService();
    private final ServiceMgmtService svcMgmt         = new ServiceMgmtService();
    private final BookingService     bookingService  = new BookingService();
    private final UserService        userService     = new UserService();
    private final AuthService        authService     = new AuthService();

    public AdminMenu(User currentUser) { this.currentUser = currentUser; }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("MENU QUẢN TRỊ VIÊN – " + currentUser.getFullName());
            System.out.println("  1. Quản lý Phòng họp");
            System.out.println("  2. Quản lý Thiết bị di động");
            System.out.println("  3. Quản lý Dịch vụ đi kèm");
            System.out.println("  4. Quản lý Người dùng");
            System.out.println("  5. Duyệt & Phân công Booking");
            System.out.println("  6. Xem tất cả Booking");
            System.out.println("  0. Đăng xuất");
            ConsoleUtil.printSeparator();
            int choice = ConsoleUtil.readChoice("Chọn: ");

            try {
                switch (choice) {
                    case 1 -> roomMenu();
                    case 2 -> equipmentMenu();
                    case 3 -> serviceMenu();
                    case 4 -> userMenu();
                    case 5 -> approvalMenu();
                    case 6 -> viewAllBookings();
                    case 0 -> { ConsoleUtil.info("Đã đăng xuất."); return; }
                    default -> ConsoleUtil.error("Lựa chọn không hợp lệ.");
                }
            } catch (SQLException e) {
                ConsoleUtil.error("Lỗi hệ thống: " + e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  A. QUẢN LÝ PHÒNG HỌP
    // ══════════════════════════════════════════════════════════════
    private void roomMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUẢN LÝ PHÒNG HỌP");
            System.out.println("  1. Danh sách phòng họp");
            System.out.println("  2. Thêm phòng mới");
            System.out.println("  3. Sửa thông tin phòng");
            System.out.println("  4. Xóa phòng");
            System.out.println("  5. Tìm kiếm phòng theo tên");
            System.out.println("  0. Quay lại");
            int c = ConsoleUtil.readChoice("Chọn: ");
            switch (c) {
                case 1 -> listRooms();
                case 2 -> addRoom();
                case 3 -> editRoom();
                case 4 -> deleteRoom();
                case 5 -> searchRoom();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Không hợp lệ.");
            }
        }
    }

    private void listRooms() throws SQLException {
        ConsoleUtil.printHeader("DANH SÁCH PHÒNG HỌP");
        List<Room> list = roomService.getAllRooms();
        String[] h = {"ID","Tên phòng","Sức chứa","Vị trí","Thiết bị cố định"};
        int[] w = {4,22,9,22,26};
        String[][] rows = list.stream().map(r -> new String[]{
            String.valueOf(r.getId()), r.getName(), String.valueOf(r.getCapacity()),
            r.getLocation(), r.getFixedEquipment()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, rows);
        ConsoleUtil.pause();
    }

    private void addRoom() throws SQLException {
        ConsoleUtil.printHeader("THÊM PHÒNG HỌP");
        String name    = ConsoleUtil.inputString("Tên phòng      : ");
        int    cap     = ConsoleUtil.inputInt("Sức chứa       : ");
        String loc     = ConsoleUtil.inputOptional("Vị trí         : ");
        String fixedEq = ConsoleUtil.inputOptional("Thiết bị cố định: ");
        String err = roomService.addRoom(name, cap, loc, fixedEq);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Thêm phòng '" + name + "' thành công!");
        ConsoleUtil.pause();
    }

    private void editRoom() throws SQLException {
        ConsoleUtil.printHeader("SỬA PHÒNG HỌP");
        listRooms();
        int id = ConsoleUtil.inputInt("Nhập ID phòng cần sửa: ");
        Room r = roomService.findById(id);
        if (r == null) { ConsoleUtil.error("Không tìm thấy phòng ID=" + id); ConsoleUtil.pause(); return; }

        ConsoleUtil.info("Giữ Enter để không thay đổi giá trị cũ.");
        String name    = ConsoleUtil.inputOptional("Tên phòng (hiện: " + r.getName() + "): ");
        String capStr  = ConsoleUtil.inputOptional("Sức chứa (hiện: " + r.getCapacity() + "): ");
        String loc     = ConsoleUtil.inputOptional("Vị trí (hiện: " + r.getLocation() + "): ");
        String fixedEq = ConsoleUtil.inputOptional("Thiết bị cố định (hiện: " + r.getFixedEquipment() + "): ");

        String newName    = name.isBlank()   ? r.getName()           : name;
        int    newCap     = capStr.isBlank() ? r.getCapacity()       : Integer.parseInt(capStr);
        String newLoc     = loc.isBlank()    ? r.getLocation()       : loc;
        String newFix     = fixedEq.isBlank()? r.getFixedEquipment() : fixedEq;

        String err = roomService.updateRoom(id, newName, newCap, newLoc, newFix);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cập nhật phòng thành công!");
        ConsoleUtil.pause();
    }

    private void deleteRoom() throws SQLException {
        ConsoleUtil.printHeader("XÓA PHÒNG HỌP");
        listRooms();
        int id = ConsoleUtil.inputInt("Nhập ID phòng cần xóa: ");
        Room r = roomService.findById(id);
        if (r == null) { ConsoleUtil.error("Không tìm thấy phòng."); ConsoleUtil.pause(); return; }
        if (!ConsoleUtil.confirm("Xác nhận xóa phòng '" + r.getName() + "'?")) {
            ConsoleUtil.info("Đã hủy."); ConsoleUtil.pause(); return;
        }
        String err = roomService.deleteRoom(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đã xóa phòng '" + r.getName() + "'.");
        ConsoleUtil.pause();
    }

    private void searchRoom() throws SQLException {
        ConsoleUtil.printHeader("TÌM KIẾM PHÒNG HỌP");
        String keyword = ConsoleUtil.inputString("Nhập từ khóa tìm kiếm: ");
        List<Room> list = roomService.searchRooms(keyword);
        String[] h = {"ID","Tên phòng","Sức chứa","Vị trí"};
        int[] w = {4,24,9,24};
        String[][] rows = list.stream().map(r -> new String[]{
            String.valueOf(r.getId()), r.getName(),
            String.valueOf(r.getCapacity()), r.getLocation()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, rows);
        ConsoleUtil.pause();
    }

    // ══════════════════════════════════════════════════════════════
    //  B. QUẢN LÝ THIẾT BỊ DI ĐỘNG
    // ══════════════════════════════════════════════════════════════
    private void equipmentMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUẢN LÝ THIẾT BỊ DI ĐỘNG");
            System.out.println("  1. Danh sách thiết bị");
            System.out.println("  2. Thêm thiết bị");
            System.out.println("  3. Sửa thiết bị");
            System.out.println("  4. Xóa thiết bị");
            System.out.println("  0. Quay lại");
            int c = ConsoleUtil.readChoice("Chọn: ");
            switch (c) {
                case 1 -> listEquipment();
                case 2 -> addEquipment();
                case 3 -> editEquipment();
                case 4 -> deleteEquipment();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Không hợp lệ.");
            }
        }
    }

    private void listEquipment() throws SQLException {
        ConsoleUtil.printHeader("DANH SÁCH THIẾT BỊ");
        List<Equipment> list = equipService.getAll();
        String[] h = {"ID","Tên thiết bị","Tổng","Khả dụng","Trạng thái"};
        int[] w = {4,28,6,9,12};
        String[][] rows = list.stream().map(e -> new String[]{
            String.valueOf(e.getId()), e.getName(),
            String.valueOf(e.getTotalQuantity()),
            String.valueOf(e.getAvailableQty()), e.getStatus()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, rows);
        ConsoleUtil.pause();
    }

    private void addEquipment() throws SQLException {
        ConsoleUtil.printHeader("THÊM THIẾT BỊ");
        String name = ConsoleUtil.inputString("Tên thiết bị: ");
        int qty     = ConsoleUtil.inputInt("Số lượng    : ");
        String err  = equipService.addEquipment(name, qty);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Thêm thiết bị '" + name + "' thành công!");
        ConsoleUtil.pause();
    }

    private void editEquipment() throws SQLException {
        ConsoleUtil.printHeader("SỬA THIẾT BỊ");
        listEquipment();
        int id = ConsoleUtil.inputInt("Nhập ID thiết bị cần sửa: ");
        Equipment eq = equipService.findById(id);
        if (eq == null) { ConsoleUtil.error("Không tìm thấy."); ConsoleUtil.pause(); return; }

        ConsoleUtil.info("Giữ Enter để không thay đổi.");
        String name    = ConsoleUtil.inputOptional("Tên (hiện: " + eq.getName() + "): ");
        String totStr  = ConsoleUtil.inputOptional("Tổng SL (hiện: " + eq.getTotalQuantity() + "): ");
        String avaStr  = ConsoleUtil.inputOptional("Khả dụng (hiện: " + eq.getAvailableQty() + "): ");
        System.out.println("Trạng thái: 1=ACTIVE  2=INACTIVE  3=MAINTENANCE");
        String statusIn = ConsoleUtil.inputOptional("Trạng thái (hiện: " + eq.getStatus() + "): ");

        String newName   = name.isBlank()    ? eq.getName()           : name;
        int    newTotal  = totStr.isBlank()  ? eq.getTotalQuantity()  : Integer.parseInt(totStr);
        int    newAvail  = avaStr.isBlank()  ? eq.getAvailableQty()   : Integer.parseInt(avaStr);
        String newStatus = switch (statusIn) {
            case "2" -> "INACTIVE";
            case "3" -> "MAINTENANCE";
            default  -> eq.getStatus().equals("ACTIVE") ? "ACTIVE" : eq.getStatus();
        };
        if (statusIn.equals("1")) newStatus = "ACTIVE";

        String err = equipService.updateEquipment(id, newName, newTotal, newAvail, newStatus);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cập nhật thiết bị thành công!");
        ConsoleUtil.pause();
    }

    private void deleteEquipment() throws SQLException {
        ConsoleUtil.printHeader("XÓA THIẾT BỊ");
        listEquipment();
        int id = ConsoleUtil.inputInt("Nhập ID thiết bị cần xóa: ");
        Equipment eq = equipService.findById(id);
        if (eq == null) { ConsoleUtil.error("Không tìm thấy."); ConsoleUtil.pause(); return; }
        if (!ConsoleUtil.confirm("Xác nhận xóa '" + eq.getName() + "'?")) {
            ConsoleUtil.info("Đã hủy."); ConsoleUtil.pause(); return;
        }
        String err = equipService.deleteEquipment(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đã xóa thiết bị.");
        ConsoleUtil.pause();
    }

    // ══════════════════════════════════════════════════════════════
    //  C. QUẢN LÝ DỊCH VỤ ĐI KÈM
    // ══════════════════════════════════════════════════════════════
    private void serviceMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUẢN LÝ DỊCH VỤ ĐI KÈM");
            System.out.println("  1. Danh sách dịch vụ");
            System.out.println("  2. Thêm dịch vụ");
            System.out.println("  3. Sửa dịch vụ");
            System.out.println("  4. Xóa (ẩn) dịch vụ");
            System.out.println("  0. Quay lại");
            int c = ConsoleUtil.readChoice("Chọn: ");
            switch (c) {
                case 1 -> listServices();
                case 2 -> addService();
                case 3 -> editService();
                case 4 -> deleteService();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Không hợp lệ.");
            }
        }
    }

    private void listServices() throws SQLException {
        ConsoleUtil.printHeader("DANH SÁCH DỊCH VỤ");
        List<Service> list = svcMgmt.getAll();
        String[] h = {"ID","Tên dịch vụ","Đơn giá","Đơn vị"};
        int[] w = {4,26,14,10};
        String[][] rows = list.stream().map(s -> new String[]{
            String.valueOf(s.getId()), s.getName(),
            String.format("%,.0f đ", s.getUnitPrice()), s.getUnit()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, rows);
        ConsoleUtil.pause();
    }

    private void addService() throws SQLException {
        ConsoleUtil.printHeader("THÊM DỊCH VỤ");
        String name  = ConsoleUtil.inputString("Tên dịch vụ: ");
        double price = ConsoleUtil.inputDouble("Đơn giá (đ) : ");
        String unit  = ConsoleUtil.inputOptional("Đơn vị      : ");
        String err   = svcMgmt.addService(name, price, unit);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Thêm dịch vụ '" + name + "' thành công!");
        ConsoleUtil.pause();
    }

    private void editService() throws SQLException {
        ConsoleUtil.printHeader("SỬA DỊCH VỤ");
        listServices();
        int id = ConsoleUtil.inputInt("Nhập ID dịch vụ cần sửa: ");
        Service sv = svcMgmt.findById(id);
        if (sv == null) { ConsoleUtil.error("Không tìm thấy."); ConsoleUtil.pause(); return; }

        ConsoleUtil.info("Giữ Enter để không thay đổi.");
        String name      = ConsoleUtil.inputOptional("Tên (hiện: " + sv.getName() + "): ");
        String priceStr  = ConsoleUtil.inputOptional("Đơn giá (hiện: " + sv.getUnitPrice() + "): ");
        String unit      = ConsoleUtil.inputOptional("Đơn vị (hiện: " + sv.getUnit() + "): ");

        String newName  = name.isBlank()     ? sv.getName()     : name;
        double newPrice = priceStr.isBlank() ? sv.getUnitPrice(): Double.parseDouble(priceStr);
        String newUnit  = unit.isBlank()     ? sv.getUnit()     : unit;

        String err = svcMgmt.updateService(id, newName, newPrice, newUnit);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cập nhật dịch vụ thành công!");
        ConsoleUtil.pause();
    }

    private void deleteService() throws SQLException {
        ConsoleUtil.printHeader("XÓA DỊCH VỤ");
        listServices();
        int id = ConsoleUtil.inputInt("Nhập ID dịch vụ cần xóa: ");
        Service sv = svcMgmt.findById(id);
        if (sv == null) { ConsoleUtil.error("Không tìm thấy."); ConsoleUtil.pause(); return; }
        if (!ConsoleUtil.confirm("Xác nhận ẩn dịch vụ '" + sv.getName() + "'?")) {
            ConsoleUtil.info("Đã hủy."); ConsoleUtil.pause(); return;
        }
        String err = svcMgmt.deleteService(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đã ẩn dịch vụ.");
        ConsoleUtil.pause();
    }

    // ══════════════════════════════════════════════════════════════
    //  D. QUẢN LÝ NGƯỜI DÙNG
    // ══════════════════════════════════════════════════════════════
    private void userMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUẢN LÝ NGƯỜI DÙNG");
            System.out.println("  1. Danh sách tất cả người dùng");
            System.out.println("  2. Tạo tài khoản Support Staff");
            System.out.println("  3. Tạo tài khoản Admin");
            System.out.println("  0. Quay lại");
            int c = ConsoleUtil.readChoice("Chọn: ");
            switch (c) {
                case 1 -> listUsers();
                case 2 -> createAccount(Role.SUPPORT_STAFF);
                case 3 -> createAccount(Role.ADMIN);
                case 0 -> { return; }
                default -> ConsoleUtil.error("Không hợp lệ.");
            }
        }
    }

    private void listUsers() throws SQLException {
        ConsoleUtil.printHeader("DANH SÁCH NGƯỜI DÙNG");
        List<User> list = userService.getAllUsers();
        String[] h = {"ID","Username","Họ tên","Phòng ban","Vai trò"};
        int[] w = {4,14,24,18,16};
        String[][] rows = list.stream().map(u -> new String[]{
            String.valueOf(u.getId()), u.getUsername(), u.getFullName(),
            u.getDepartment(), u.getRole().getDisplayName()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, rows);
        ConsoleUtil.pause();
    }

    private void createAccount(Role role) throws SQLException {
        ConsoleUtil.printHeader("TẠO TÀI KHOẢN " + role.getDisplayName().toUpperCase());
        String username = ConsoleUtil.inputString("Tên đăng nhập : ");
        String password = ConsoleUtil.inputPassword("Mật khẩu      : ");
        String fullName = ConsoleUtil.inputString("Họ và tên     : ");
        String email    = ConsoleUtil.inputOptional("Email         : ");
        String phone    = ConsoleUtil.inputOptional("Số điện thoại : ");
        String dept     = ConsoleUtil.inputOptional("Phòng ban     : ");

        String err = authService.createAccount(username, password, fullName, email, phone, dept, role);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Tạo tài khoản '" + username + "' thành công!");
        ConsoleUtil.pause();
    }

    // ══════════════════════════════════════════════════════════════
    //  E. DUYỆT & PHÂN CÔNG BOOKING
    // ══════════════════════════════════════════════════════════════
    private void approvalMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("DUYỆT & PHÂN CÔNG BOOKING");
            System.out.println("  1. Danh sách booking PENDING");
            System.out.println("  2. Duyệt booking");
            System.out.println("  3. Từ chối booking");
            System.out.println("  4. Phân công nhân viên hỗ trợ");
            System.out.println("  0. Quay lại");
            int c = ConsoleUtil.readChoice("Chọn: ");
            switch (c) {
                case 1 -> listPending();
                case 2 -> approveBooking();
                case 3 -> rejectBooking();
                case 4 -> assignStaff();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Không hợp lệ.");
            }
        }
    }

    private void listPending() throws SQLException {
        ConsoleUtil.printHeader("BOOKING ĐANG CHỜ DUYỆT");
        BookingHelper.printBookingList(bookingService.getPendingBookings());
        ConsoleUtil.pause();
    }

    private void approveBooking() throws SQLException {
        ConsoleUtil.printHeader("DUYỆT BOOKING");
        BookingHelper.printBookingList(bookingService.getPendingBookings());
        int id = ConsoleUtil.inputInt("Nhập ID booking cần duyệt: ");

        Booking b = bookingService.findById(id);
        if (b != null) BookingHelper.printBookingDetail(b);

        if (!ConsoleUtil.confirm("Xác nhận DUYỆT booking #" + id + "?")) {
            ConsoleUtil.info("Đã hủy."); ConsoleUtil.pause(); return;
        }
        String err = bookingService.approveBooking(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đã duyệt booking #" + id);
        ConsoleUtil.pause();
    }

    private void rejectBooking() throws SQLException {
        ConsoleUtil.printHeader("TỪ CHỐI BOOKING");
        BookingHelper.printBookingList(bookingService.getPendingBookings());
        int id = ConsoleUtil.inputInt("Nhập ID booking cần từ chối: ");
        String reason = ConsoleUtil.inputString("Lý do từ chối : ");

        if (!ConsoleUtil.confirm("Xác nhận TỪ CHỐI booking #" + id + "?")) {
            ConsoleUtil.info("Đã hủy."); ConsoleUtil.pause(); return;
        }
        String err = bookingService.rejectBooking(id, reason);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đã từ chối booking #" + id);
        ConsoleUtil.pause();
    }

    private void assignStaff() throws SQLException {
        ConsoleUtil.printHeader("PHÂN CÔNG NHÂN VIÊN HỖ TRỢ");
        // Hiện booking đã duyệt chưa có nhân viên (lấy từ all approved)
        List<Booking> approved = bookingService.getAllBookings().stream()
                .filter(b -> b.getStatus() == Booking.Status.APPROVED)
                .toList();
        BookingHelper.printBookingList(approved);
        if (approved.isEmpty()) { ConsoleUtil.pause(); return; }

        int bookingId = ConsoleUtil.inputInt("Nhập ID booking cần phân công: ");

        // Hiển thị danh sách support staff
        List<User> staffList = userService.getUsersByRole(Role.SUPPORT_STAFF);
        if (staffList.isEmpty()) {
            ConsoleUtil.warn("Chưa có nhân viên hỗ trợ nào."); ConsoleUtil.pause(); return;
        }
        String[] h = {"ID","Username","Họ tên","Phòng ban"};
        int[] w = {4,14,24,18};
        String[][] rows = staffList.stream().map(u -> new String[]{
            String.valueOf(u.getId()), u.getUsername(), u.getFullName(), u.getDepartment()
        }).toArray(String[][]::new);
        ConsoleUtil.printTable(h, w, rows);

        int staffId = ConsoleUtil.inputInt("Nhập ID nhân viên hỗ trợ: ");
        User staff = userService.findById(staffId);
        if (staff == null || staff.getRole() != Role.SUPPORT_STAFF) {
            ConsoleUtil.error("Nhân viên hỗ trợ không tồn tại."); ConsoleUtil.pause(); return;
        }

        String err = bookingService.assignStaff(bookingId, staffId);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Đã phân công " + staff.getFullName() + " cho booking #" + bookingId);
        ConsoleUtil.pause();
    }

    // ══════════════════════════════════════════════════════════════
    //  F. XEM TẤT CẢ BOOKING
    // ══════════════════════════════════════════════════════════════
    private void viewAllBookings() throws SQLException {
        ConsoleUtil.printHeader("TẤT CẢ BOOKING");
        List<Booking> list = bookingService.getAllBookings();
        BookingHelper.printBookingList(list);

        if (!list.isEmpty()) {
            String choice = ConsoleUtil.inputOptional("\nNhập ID booking để xem chi tiết (Enter để bỏ qua): ");
            if (!choice.isBlank()) {
                try {
                    int id = Integer.parseInt(choice);
                    Booking b = bookingService.findById(id);
                    if (b != null) BookingHelper.printBookingDetail(b);
                    else ConsoleUtil.error("Không tìm thấy booking #" + id);
                } catch (NumberFormatException e) {
                    ConsoleUtil.error("ID không hợp lệ.");
                }
            }
        }
        ConsoleUtil.pause();
    }
}
