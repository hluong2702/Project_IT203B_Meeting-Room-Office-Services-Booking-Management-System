package presentation;

import model.*;
import service.*;
import util.ConsoleUtil;
import util.Validate;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EmployeeMenu {
    private final User currentUser;
    private final BookingService bookingService  = new BookingService();
    private final RoomService roomService     = new RoomService();
    private final EquipmentService equipmentService = new EquipmentService();
    private final ServiceMgmtService serviceMgmt     = new ServiceMgmtService();
    private final AuthService authService     = new AuthService();

    public EmployeeMenu(User currentUser) { this.currentUser = currentUser; }

    public void show() {
        while (true) {
            ConsoleUtil.printHeader("MENU NHAN VIEN - " + currentUser.getFullName());
            System.out.println("1. Dat phong hop moi");
            System.out.println("2. Xem lich hop cua toi");
            System.out.println("3. Chi tiet / Huy booking");
            System.out.println("4. Ho so ca nhan");
            System.out.println("5. Doi mat khau");
            System.out.println("0. Dang xuat");
            int choice = ConsoleUtil.readChoice("Chon: ");
            try {
                switch (choice) {
                    case 1 -> createBooking();
                    case 2 -> viewMyBookings();
                    case 3 -> cancelBookingMenu();
                    case 4 -> updateProfile();
                    case 5 -> changePassword();
                    case 0 -> { ConsoleUtil.info("Da dang xuat."); return; }
                    default -> ConsoleUtil.error("Lua chon khong hop le.");
                }
            } catch (SQLException e) {
                ConsoleUtil.error("Loi he thong: " + e.getMessage());
            }
        }
    }

    private void createBooking() throws SQLException {
        ConsoleUtil.printHeader("DAT PHONG HOP");
        List<Room> rooms = roomService.getAllRooms();
        if (rooms.isEmpty()) { ConsoleUtil.warn("Hien chua co phong hop nao."); ConsoleUtil.pause(); return; }

        ConsoleUtil.printSeparator();
        for (Room r : rooms)
            System.out.printf("ID: %-3d | %-20s | Suc chua: %-3d | Vi tri: %s%n",
                    r.getId(), r.getName(), r.getCapacity(), r.getLocation());
        ConsoleUtil.printSeparator();

        int roomId = ConsoleUtil.inputInt("Nhap ID phong: ");
        Room room  = roomService.findById(roomId);
        if (room == null) { ConsoleUtil.error("Phong khong ton tai."); ConsoleUtil.pause(); return; }

        String title = ConsoleUtil.inputString("Tieu de cuoc hop: ");
        LocalDateTime start = ConsoleUtil.inputDateTime("Thoi gian bat dau");
        if (!Validate.isFutureTime(start)) {
            ConsoleUtil.error("Thoi gian bat dau phai o tuong lai."); ConsoleUtil.pause(); return;
        }
        LocalDateTime end = ConsoleUtil.inputDateTime("Thoi gian ket thuc");
        if (!Validate.isEndAfterStart(start, end)) {
            ConsoleUtil.error("Thoi gian ket thuc phai sau bat dau."); ConsoleUtil.pause(); return;
        }
        int attendees = ConsoleUtil.inputInt("So nguoi tham du: ");
        String note   = ConsoleUtil.inputOptional("Ghi chu (Enter de bo qua): ");

        Map<Integer, Integer> equipmentMap = selectEquipment();
        Map<Integer, Integer> serviceMap   = selectServices();

        System.out.printf("Xac nhan dat phong '%s' tu %s den %s cho %d nguoi?%n",
                room.getName(), start.format(ConsoleUtil.DATE_FMT),
                end.format(ConsoleUtil.DATE_FMT), attendees);
        if (!ConsoleUtil.confirm("Xac nhan")) { ConsoleUtil.info("Da huy."); ConsoleUtil.pause(); return; }

        String err = bookingService.createBooking(currentUser.getId(), roomId, title,
                start, end, attendees, note, equipmentMap, serviceMap);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Dat phong thanh cong! Booking dang cho Admin duyet.");
        ConsoleUtil.pause();
    }

    private Map<Integer, Integer> selectEquipment() throws SQLException {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        List<Equipment> list = equipmentService.getActive();
        if (list.isEmpty()) return map;

        System.out.println("\n--- THIET BI DI DONG KHA DUNG ---");
        for (Equipment e : list)
            System.out.printf("ID: %-3d | %-25s | Kha dung: %d%n",
                    e.getId(), e.getName(), e.getAvailableQty());

        System.out.println("Nhap thiet bi can muon (dinh dang: ID,so_luong | nhap 0 de ket thuc):");
        while (true) {
            String input = ConsoleUtil.inputOptional("  > ");
            if (input.equals("0") || input.isBlank()) break;
            try {
                String[] parts = input.split(",");
                int eId = Integer.parseInt(parts[0].trim());
                int qty = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                Equipment eq = equipmentService.findById(eId);
                if (eq == null) { ConsoleUtil.error("Thiet bi ID=" + eId + " khong ton tai."); continue; }
                if (qty > eq.getAvailableQty()) {
                    ConsoleUtil.error("Chi con " + eq.getAvailableQty() + " cai " + eq.getName()); continue;
                }
                map.put(eId, qty);
                ConsoleUtil.success("Da them: " + eq.getName() + " x" + qty);
            } catch (Exception e) {
                ConsoleUtil.error("Dinh dang sai. Vi du: 2,1 (ID=2, so luong=1)");
            }
        }
        return map;
    }

    private Map<Integer, Integer> selectServices() throws SQLException {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        List<Service> list = serviceMgmt.getAll();
        if (list.isEmpty()) return map;

        System.out.println("\n--- DICH VU DI KEM ---");
        for (Service s : list)
            System.out.printf("ID: %-3d | %-25s | %,.0f d/%s%n",
                    s.getId(), s.getName(), s.getUnitPrice(), s.getUnit());

        System.out.println("Nhap dich vu can dat (dinh dang: ID,so_luong | nhap 0 de ket thuc):");
        while (true) {
            String input = ConsoleUtil.inputOptional("  > ");
            if (input.equals("0") || input.isBlank()) break;
            try {
                String[] parts = input.split(",");
                int sId = Integer.parseInt(parts[0].trim());
                int qty = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                Service sv = serviceMgmt.findById(sId);
                if (sv == null) { ConsoleUtil.error("Dich vu ID=" + sId + " khong ton tai."); continue; }
                map.put(sId, qty);
                ConsoleUtil.success("Da them: " + sv.getName() + " x" + qty);
            } catch (Exception e) {
                ConsoleUtil.error("Dinh dang sai. Vi du: 1,2 (ID=1, so luong=2)");
            }
        }
        return map;
    }

    private void viewMyBookings() throws SQLException {
        ConsoleUtil.printHeader("LICH HOP CUA TOI");
        BookingHelper.printBookingList(bookingService.getMyBookings(currentUser.getId()));
        ConsoleUtil.pause();
    }

    private void cancelBookingMenu() throws SQLException {
        ConsoleUtil.printHeader("CHI TIET / HUY BOOKING");
        List<Booking> list = bookingService.getMyBookings(currentUser.getId());
        BookingHelper.printBookingList(list);
        if (list.isEmpty()) { ConsoleUtil.pause(); return; }

        int id = ConsoleUtil.inputInt("Nhap ID booking de xem chi tiet: ");
        Booking b = bookingService.findById(id);
        if (b == null || b.getUserId() != currentUser.getId()) {
            ConsoleUtil.error("Khong tim thay booking hoac khong thuoc ve ban.");
            ConsoleUtil.pause(); return;
        }
        BookingHelper.printBookingDetail(b);

        if (b.getStatus() == Booking.Status.PENDING) {
            if (ConsoleUtil.confirm("Ban co muon HUY booking nay khong?")) {
                String err = bookingService.cancelBooking(id, currentUser.getId());
                if (err != null) ConsoleUtil.error(err);
                else ConsoleUtil.success("Da huy booking thanh cong.");
            }
        } else {
            ConsoleUtil.info("Booking nay khong o trang thai PENDING nen khong the huy.");
        }
        ConsoleUtil.pause();
    }

    private void updateProfile() throws SQLException {
        ConsoleUtil.printHeader("HO SO CA NHAN");
        System.out.println("Username   : " + currentUser.getUsername());
        System.out.println("Ho ten     : " + currentUser.getFullName());
        System.out.println("Email      : " + currentUser.getEmail());
        System.out.println("Dien thoai : " + currentUser.getPhone());
        System.out.println("Phong ban  : " + currentUser.getDepartment());
        System.out.println("Vai tro    : " + currentUser.getRole().getDisplayName());
        ConsoleUtil.printSeparator();

        if (!ConsoleUtil.confirm("Ban co muon cap nhat ho so khong?")) { ConsoleUtil.pause(); return; }

        String fullName   = ConsoleUtil.inputOptional("Ho ten (hien: " + currentUser.getFullName() + "): ");
        String email      = ConsoleUtil.inputOptional("Email (hien: " + currentUser.getEmail() + "): ");
        String phone      = ConsoleUtil.inputOptional("SDT (hien: " + currentUser.getPhone() + "): ");
        String department = ConsoleUtil.inputOptional("Phong ban (hien: " + currentUser.getDepartment() + "): ");

        String err = authService.updateProfile(currentUser,
                fullName.isBlank()    ? currentUser.getFullName()   : fullName,
                email.isBlank()       ? currentUser.getEmail()      : email,
                phone.isBlank()       ? currentUser.getPhone()      : phone,
                department.isBlank()  ? currentUser.getDepartment() : department);

        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Cap nhat ho so thanh cong!");
        ConsoleUtil.pause();
    }

    private void changePassword() throws SQLException {
        ConsoleUtil.printHeader("DOI MAT KHAU");
        String oldPwd  = ConsoleUtil.inputPassword("Mat khau cu  : ");
        String newPwd  = ConsoleUtil.inputPassword("Mat khau moi : ");
        String confirm = ConsoleUtil.inputPassword("Xac nhan lai : ");
        if (!newPwd.equals(confirm)) { ConsoleUtil.error("Mat khau xac nhan khong khop."); ConsoleUtil.pause(); return; }
        String err = authService.changePassword(currentUser, oldPwd, newPwd);
        if (err != null) ConsoleUtil.error(err);
        else ConsoleUtil.success("Doi mat khau thanh cong!");
        ConsoleUtil.pause();
    }
}
