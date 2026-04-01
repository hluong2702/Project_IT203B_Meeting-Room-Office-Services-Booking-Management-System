package presentation;

import model.*;
import service.*;
import util.ConsoleUtil;

import java.sql.SQLException;
import java.util.List;

public class AdminMenu {

    private final User currentUser;
    private final RoomService roomService    = new RoomService();
    private final EquipmentService equipService   = new EquipmentService();
    private final ServiceMgmtService svcMgmt        = new ServiceMgmtService();
    private final BookingService bookingService = new BookingService();
    private final UserService        userService    = new UserService();
    private final AuthService authService    = new AuthService();

    public AdminMenu(User currentUser) { this.currentUser = currentUser; }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("MENU QUAN TRI VIEN - " + currentUser.getFullName());
            System.out.println("1. Quan ly Phong hop");
            System.out.println("2. Quan ly Thiet bi di dong");
            System.out.println("3. Quan ly Dich vu di kem");
            System.out.println("4. Quan ly Nguoi dung");
            System.out.println("5. Duyet & Phan cong Booking");
            System.out.println("6. Xem tat ca Booking");
            System.out.println("0. Dang xuat");
            int choice = ConsoleUtil.readChoice("Chon: ");
            try {
                switch (choice) {
                    case 1 -> roomMenu();
                    case 2 -> equipmentMenu();
                    case 3 -> serviceMenu();
                    case 4 -> userMenu();
                    case 5 -> approvalMenu();
                    case 6 -> viewAllBookings();
                    case 0 -> { ConsoleUtil.info("Da dang xuat."); return; }
                    default -> ConsoleUtil.error("Lua chon khong hop le.");
                }
            } catch (SQLException e) {
                ConsoleUtil.error("Loi he thong: " + e.getMessage());
            }
        }
    }

    //  Phòng họp
    private void roomMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUAN LY PHONG HOP");
            System.out.println("1. Danh sach phong hop");
            System.out.println("2. Them phong moi");
            System.out.println("3. Sua thong tin phong");
            System.out.println("4. Xoa phong");
            System.out.println("5. Tim kiem phong theo ten");
            System.out.println("0. Quay lai");
            int c = ConsoleUtil.readChoice("Chon: ");
            switch (c) {
                case 1 -> listRooms();
                case 2 -> addRoom();
                case 3 -> editRoom();
                case 4 -> deleteRoom();
                case 5 -> searchRoom();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Khong hop le.");
            }
        }
    }

    private void listRooms() throws SQLException {
        ConsoleUtil.printHeader("DANH SACH PHONG HOP");
        List<Room> list = roomService.getAllRooms();
        if (list.isEmpty()) { ConsoleUtil.info("Chua co phong nao."); ConsoleUtil.pause(); return; }
        ConsoleUtil.printSeparator();
        for (Room r : list) {
            System.out.printf("ID: %-3d | Ten: %-20s | Suc chua: %-3d | Vi tri: %s%n",
                    r.getId(), r.getName(), r.getCapacity(), r.getLocation());
            if (r.getFixedEquipment() != null && !r.getFixedEquipment().isBlank())
                System.out.println("         Thiet bi co dinh: " + r.getFixedEquipment());
        }
        ConsoleUtil.printSeparator();
        ConsoleUtil.pause();
    }

    private void addRoom() throws SQLException {
        ConsoleUtil.printHeader("THEM PHONG HOP");
        String name    = ConsoleUtil.inputString("Ten phong       : ");
        int    cap     = ConsoleUtil.inputInt("Suc chua        : ");
        String loc     = ConsoleUtil.inputOptional("Vi tri          : ");
        String fixedEq = ConsoleUtil.inputOptional("Thiet bi co dinh: ");
        String err = roomService.addRoom(name, cap, loc, fixedEq);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Them phong '" + name + "' thanh cong!");
        ConsoleUtil.pause();
    }

    private void editRoom() throws SQLException {
        ConsoleUtil.printHeader("SUA PHONG HOP");
        listRooms();
        int id = ConsoleUtil.inputInt("Nhap ID phong can sua: ");
        Room r = roomService.findById(id);
        if (r == null) { ConsoleUtil.error("Khong tim thay phong ID=" + id); ConsoleUtil.pause(); return; }

        ConsoleUtil.info("Nhan Enter de giu nguyen gia tri cu.");
        String name    = ConsoleUtil.inputOptional("Ten phong (hien: " + r.getName() + "): ");
        String capStr  = ConsoleUtil.inputOptional("Suc chua (hien: " + r.getCapacity() + "): ");
        String loc     = ConsoleUtil.inputOptional("Vi tri (hien: " + r.getLocation() + "): ");
        String fixedEq = ConsoleUtil.inputOptional("Thiet bi co dinh (hien: " + r.getFixedEquipment() + "): ");

        String newName = name.isBlank()    ? r.getName()           : name;
        int    newCap  = capStr.isBlank()  ? r.getCapacity()       : Integer.parseInt(capStr);
        String newLoc  = loc.isBlank()     ? r.getLocation()       : loc;
        String newFix  = fixedEq.isBlank() ? r.getFixedEquipment() : fixedEq;

        String err = roomService.updateRoom(id, newName, newCap, newLoc, newFix);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cap nhat phong thanh cong!");
        ConsoleUtil.pause();
    }

    private void deleteRoom() throws SQLException {
        ConsoleUtil.printHeader("XOA PHONG HOP");
        listRooms();
        int id = ConsoleUtil.inputInt("Nhap ID phong can xoa: ");
        Room r = roomService.findById(id);
        if (r == null) { ConsoleUtil.error("Khong tim thay phong."); ConsoleUtil.pause(); return; }
        if (!ConsoleUtil.confirm("Xac nhan xoa phong '" + r.getName() + "'?")) {
            ConsoleUtil.info("Da huy."); ConsoleUtil.pause(); return;
        }
        String err = roomService.deleteRoom(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Da xoa phong '" + r.getName() + "'.");
        ConsoleUtil.pause();
    }

    private void searchRoom() throws SQLException {
        ConsoleUtil.printHeader("TIM KIEM PHONG HOP");
        String keyword = ConsoleUtil.inputString("Nhap tu khoa: ");
        List<Room> list = roomService.searchRooms(keyword);
        if (list.isEmpty()) { ConsoleUtil.info("Khong tim thay phong nao."); ConsoleUtil.pause(); return; }
        ConsoleUtil.printSeparator();
        for (Room r : list)
            System.out.printf("ID: %-3d | %-20s | Suc chua: %-3d | %s%n",
                    r.getId(), r.getName(), r.getCapacity(), r.getLocation());
        ConsoleUtil.printSeparator();
        ConsoleUtil.pause();
    }

    //  Thiet bi di dong
    private void equipmentMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUAN LY THIET BI DI DONG");
            System.out.println("1. Danh sach thiet bi");
            System.out.println("2. Them thiet bi");
            System.out.println("3. Sua thiet bi");
            System.out.println("4. Xoa thiet bi");
            System.out.println("0. Quay lai");
            int c = ConsoleUtil.readChoice("Chon: ");
            switch (c) {
                case 1 -> listEquipment();
                case 2 -> addEquipment();
                case 3 -> editEquipment();
                case 4 -> deleteEquipment();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Khong hop le.");
            }
        }
    }

    private void listEquipment() throws SQLException {
        ConsoleUtil.printHeader("DANH SACH THIET BI");
        List<Equipment> list = equipService.getAll();
        if (list.isEmpty()) { ConsoleUtil.info("Chua co thiet bi nao."); ConsoleUtil.pause(); return; }
        ConsoleUtil.printSeparator();
        for (Equipment e : list)
            System.out.printf("ID: %-3d | %-25s | Tong: %-3d | Kha dung: %-3d | %s%n",
                    e.getId(), e.getName(), e.getTotalQuantity(), e.getAvailableQty(), e.getStatus());
        ConsoleUtil.printSeparator();
        ConsoleUtil.pause();
    }

    private void addEquipment() throws SQLException {
        ConsoleUtil.printHeader("THEM THIET BI");
        String name = ConsoleUtil.inputString("Ten thiet bi: ");
        int qty     = ConsoleUtil.inputInt("So luong    : ");
        String err  = equipService.addEquipment(name, qty);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Them thiet bi '" + name + "' thanh cong!");
        ConsoleUtil.pause();
    }

    private void editEquipment() throws SQLException {
        ConsoleUtil.printHeader("SUA THIET BI");
        listEquipment();
        int id = ConsoleUtil.inputInt("Nhap ID thiet bi can sua: ");
        Equipment eq = equipService.findById(id);
        if (eq == null) { ConsoleUtil.error("Khong tim thay."); ConsoleUtil.pause(); return; }

        ConsoleUtil.info("Nhan Enter de giu nguyen gia tri cu.");
        String name     = ConsoleUtil.inputOptional("Ten (hien: " + eq.getName() + "): ");
        String totStr   = ConsoleUtil.inputOptional("Tong SL (hien: " + eq.getTotalQuantity() + "): ");
        String avaStr   = ConsoleUtil.inputOptional("Kha dung (hien: " + eq.getAvailableQty() + "): ");
        System.out.println("Trang thai: 1=ACTIVE  2=INACTIVE  3=MAINTENANCE");
        String stIn     = ConsoleUtil.inputOptional("Trang thai (hien: " + eq.getStatus() + "): ");

        String newName   = name.isBlank()   ? eq.getName()          : name;
        int    newTotal  = totStr.isBlank() ? eq.getTotalQuantity() : Integer.parseInt(totStr);
        int    newAvail  = avaStr.isBlank() ? eq.getAvailableQty()  : Integer.parseInt(avaStr);
        String newStatus = switch (stIn) {
            case "2" -> "INACTIVE";
            case "3" -> "MAINTENANCE";
            case "1" -> "ACTIVE";
            default  -> eq.getStatus();
        };

        String err = equipService.updateEquipment(id, newName, newTotal, newAvail, newStatus);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cap nhat thiet bi thanh cong!");
        ConsoleUtil.pause();
    }

    private void deleteEquipment() throws SQLException {
        ConsoleUtil.printHeader("XOA THIET BI");
        listEquipment();
        int id = ConsoleUtil.inputInt("Nhap ID thiet bi can xoa: ");
        Equipment eq = equipService.findById(id);
        if (eq == null) { ConsoleUtil.error("Khong tim thay."); ConsoleUtil.pause(); return; }
        if (!ConsoleUtil.confirm("Xac nhan xoa '" + eq.getName() + "'?")) {
            ConsoleUtil.info("Da huy."); ConsoleUtil.pause(); return;
        }
        String err = equipService.deleteEquipment(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Da xoa thiet bi.");
        ConsoleUtil.pause();
    }

    //  Dich vu di kem
    private void serviceMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUAN LY DICH VU DI KEM");
            System.out.println("1. Danh sach dich vu");
            System.out.println("2. Them dich vu");
            System.out.println("3. Sua dich vu");
            System.out.println("4. Xoa dich vu");
            System.out.println("0. Quay lai");
            int c = ConsoleUtil.readChoice("Chon: ");
            switch (c) {
                case 1 -> listServices();
                case 2 -> addService();
                case 3 -> editService();
                case 4 -> deleteService();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Khong hop le.");
            }
        }
    }

    private void listServices() throws SQLException {
        ConsoleUtil.printHeader("DANH SACH DICH VU");
        List<Service> list = svcMgmt.getAll();
        if (list.isEmpty()) { ConsoleUtil.info("Chua co dich vu nao."); ConsoleUtil.pause(); return; }
        ConsoleUtil.printSeparator();
        for (Service s : list)
            System.out.printf("ID: %-3d | %-25s | %,.0f d/%s%n",
                    s.getId(), s.getName(), s.getUnitPrice(), s.getUnit());
        ConsoleUtil.printSeparator();
        ConsoleUtil.pause();
    }

    private void addService() throws SQLException {
        ConsoleUtil.printHeader("THEM DICH VU");
        String name  = ConsoleUtil.inputString("Ten dich vu: ");
        double price = ConsoleUtil.inputDouble("Don gia (d) : ");
        String unit  = ConsoleUtil.inputOptional("Don vi      : ");
        String err   = svcMgmt.addService(name, price, unit);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Them dich vu '" + name + "' thanh cong!");
        ConsoleUtil.pause();
    }

    private void editService() throws SQLException {
        ConsoleUtil.printHeader("SUA DICH VU");
        listServices();
        int id = ConsoleUtil.inputInt("Nhap ID dich vu can sua: ");
        Service sv = svcMgmt.findById(id);
        if (sv == null) { ConsoleUtil.error("Khong tim thay."); ConsoleUtil.pause(); return; }

        ConsoleUtil.info("Nhan Enter de giu nguyen gia tri cu.");
        String name     = ConsoleUtil.inputOptional("Ten (hien: " + sv.getName() + "): ");
        String priceStr = ConsoleUtil.inputOptional("Don gia (hien: " + sv.getUnitPrice() + "): ");
        String unit     = ConsoleUtil.inputOptional("Don vi (hien: " + sv.getUnit() + "): ");

        String newName  = name.isBlank()     ? sv.getName()      : name;
        double newPrice = priceStr.isBlank() ? sv.getUnitPrice() : Double.parseDouble(priceStr);
        String newUnit  = unit.isBlank()     ? sv.getUnit()      : unit;

        String err = svcMgmt.updateService(id, newName, newPrice, newUnit);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cap nhat dich vu thanh cong!");
        ConsoleUtil.pause();
    }

    private void deleteService() throws SQLException {
        ConsoleUtil.printHeader("XOA DICH VU");
        listServices();
        int id = ConsoleUtil.inputInt("Nhap ID dich vu can xoa: ");
        Service sv = svcMgmt.findById(id);
        if (sv == null) { ConsoleUtil.error("Khong tim thay."); ConsoleUtil.pause(); return; }
        if (!ConsoleUtil.confirm("Xac nhan an dich vu '" + sv.getName() + "'?")) {
            ConsoleUtil.info("Da huy."); ConsoleUtil.pause(); return;
        }
        String err = svcMgmt.deleteService(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Da an dich vu.");
        ConsoleUtil.pause();
    }

    //  Nguoi dung
    private void userMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("QUAN LY NGUOI DUNG");
            System.out.println("1. Danh sach tat ca nguoi dung");
            System.out.println("2. Tao tai khoan Support Staff");
            System.out.println("3. Tao tai khoan Admin");
            System.out.println("0. Quay lai");
            int c = ConsoleUtil.readChoice("Chon: ");
            switch (c) {
                case 1 -> listUsers();
                case 2 -> createAccount(Role.SUPPORT_STAFF);
                case 3 -> createAccount(Role.ADMIN);
                case 0 -> { return; }
                default -> ConsoleUtil.error("Khong hop le.");
            }
        }
    }

    private void listUsers() throws SQLException {
        ConsoleUtil.printHeader("DANH SACH NGUOI DUNG");
        List<User> list = userService.getAllUsers();
        if (list.isEmpty()) { ConsoleUtil.info("Chua co nguoi dung nao."); ConsoleUtil.pause(); return; }
        ConsoleUtil.printSeparator();
        for (User u : list)
            System.out.printf("ID: %-3d | %-15s | %-25s | %-15s | %s%n",
                    u.getId(), u.getUsername(), u.getFullName(),
                    u.getDepartment(), u.getRole().getDisplayName());
        ConsoleUtil.printSeparator();
        ConsoleUtil.pause();
    }

    private void createAccount(Role role) throws SQLException {
        ConsoleUtil.printHeader("TAO TAI KHOAN " + role.getDisplayName().toUpperCase());
        String username = ConsoleUtil.inputString("Ten dang nhap : ");
        String password = ConsoleUtil.inputPassword("Mat khau      : ");
        String fullName = ConsoleUtil.inputString("Ho va ten     : ");
        String email    = ConsoleUtil.inputOptional("Email         : ");
        String phone    = ConsoleUtil.inputOptional("So dien thoai : ");
        String dept     = ConsoleUtil.inputOptional("Phong ban     : ");
        String err = authService.createAccount(username, password, fullName, email, phone, dept, role);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Tao tai khoan '" + username + "' thanh cong!");
        ConsoleUtil.pause();
    }

    //  Duyet va phan cong
    private void approvalMenu() throws SQLException {
        while (true) {
            ConsoleUtil.printHeader("DUYET & PHAN CONG BOOKING");
            System.out.println("1. Danh sach booking PENDING");
            System.out.println("2. Duyet booking");
            System.out.println("3. Tu choi booking");
            System.out.println("4. Phan cong nhan vien ho tro");
            System.out.println("0. Quay lai");
            int c = ConsoleUtil.readChoice("Chon: ");
            switch (c) {
                case 1 -> listPending();
                case 2 -> approveBooking();
                case 3 -> rejectBooking();
                case 4 -> assignStaff();
                case 0 -> { return; }
                default -> ConsoleUtil.error("Khong hop le.");
            }
        }
    }

    private void listPending() throws SQLException {
        ConsoleUtil.printHeader("BOOKING DANG CHO DUYET");
        BookingHelper.printBookingList(bookingService.getPendingBookings());
        ConsoleUtil.pause();
    }

    private void approveBooking() throws SQLException {
        ConsoleUtil.printHeader("DUYET BOOKING");
        BookingHelper.printBookingList(bookingService.getPendingBookings());
        int id = ConsoleUtil.inputInt("Nhap ID booking can duyet: ");
        Booking b = bookingService.findById(id);
        if (b != null) BookingHelper.printBookingDetail(b);
        if (!ConsoleUtil.confirm("Xac nhan DUYET booking #" + id + "?")) {
            ConsoleUtil.info("Da huy."); ConsoleUtil.pause(); return;
        }
        String err = bookingService.approveBooking(id);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Da duyet booking #" + id);
        ConsoleUtil.pause();
    }

    private void rejectBooking() throws SQLException {
        ConsoleUtil.printHeader("TU CHOI BOOKING");
        BookingHelper.printBookingList(bookingService.getPendingBookings());
        int id     = ConsoleUtil.inputInt("Nhap ID booking can tu choi: ");
        String reason = ConsoleUtil.inputString("Ly do tu choi: ");
        if (!ConsoleUtil.confirm("Xac nhan TU CHOI booking #" + id + "?")) {
            ConsoleUtil.info("Da huy."); ConsoleUtil.pause(); return;
        }
        String err = bookingService.rejectBooking(id, reason);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Da tu choi booking #" + id);
        ConsoleUtil.pause();
    }

    private void assignStaff() throws SQLException {
        ConsoleUtil.printHeader("PHAN CONG NHAN VIEN HO TRO");
        List<Booking> approved = bookingService.getAllBookings().stream()
                .filter(b -> b.getStatus() == Booking.Status.APPROVED)
                .toList();
        BookingHelper.printBookingList(approved);
        if (approved.isEmpty()) { ConsoleUtil.pause(); return; }

        int bookingId = ConsoleUtil.inputInt("Nhap ID booking can phan cong: ");

        List<User> staffList = userService.getUsersByRole(Role.SUPPORT_STAFF);
        if (staffList.isEmpty()) {
            ConsoleUtil.warn("Chua co nhan vien ho tro nao."); ConsoleUtil.pause(); return;
        }
        ConsoleUtil.printSeparator();
        for (User u : staffList)
            System.out.printf("ID: %-3d | %-15s | %-25s | %s%n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getDepartment());
        ConsoleUtil.printSeparator();

        int staffId = ConsoleUtil.inputInt("Nhap ID nhan vien ho tro: ");
        User staff = userService.findById(staffId);
        if (staff == null || staff.getRole() != Role.SUPPORT_STAFF) {
            ConsoleUtil.error("Nhan vien ho tro khong ton tai."); ConsoleUtil.pause(); return;
        }
        String err = bookingService.assignStaff(bookingId, staffId);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Da phan cong " + staff.getFullName() + " cho booking #" + bookingId);
        ConsoleUtil.pause();
    }

    //  Xem tat ca booking
    private void viewAllBookings() throws SQLException {
        ConsoleUtil.printHeader("TAT CA BOOKING");
        List<Booking> list = bookingService.getAllBookings();
        BookingHelper.printBookingList(list);
        if (!list.isEmpty()) {
            String input = ConsoleUtil.inputOptional("Nhap ID booking de xem chi tiet (Enter de bo qua): ");
            if (!input.isBlank()) {
                try {
                    Booking b = bookingService.findById(Integer.parseInt(input));
                    if (b != null) BookingHelper.printBookingDetail(b);
                    else ConsoleUtil.error("Khong tim thay booking #" + input);
                } catch (NumberFormatException e) {
                    ConsoleUtil.error("ID khong hop le.");
                }
            }
        }
        ConsoleUtil.pause();
    }

}
