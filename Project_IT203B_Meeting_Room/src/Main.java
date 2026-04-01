import model.User;
import presentation.AdminMenu;
import presentation.AuthMenu;
import presentation.EmployeeMenu;
import presentation.SupportStaffMenu;
import util.ConsoleUtil;
import util.JDBCConnection;
import util.PasswordHash;

public class Main {
    public static void main(String[] args) {

        // Khởi tạo Singleton JDBCConnection lần đầu
        try {
            JDBCConnection.getInstance();
            ConsoleUtil.success("Ket noi co so du lieu thanh cong!");
        } catch (Exception e) {
            ConsoleUtil.error("Khong the ket noi CSDL: " + e.getMessage());
            ConsoleUtil.error("Kiem tra cau hinh trong JDBCConnection.java va dam bao MySQL dang chay.");
            return;
        }

        // Vòng lặp chính: đăng nhập → dùng hệ thống → đăng xuất
        AuthMenu authMenu = new AuthMenu();
        while (true) {
            User loggedInUser = authMenu.show();

            if (loggedInUser == null) {
                ConsoleUtil.info("Cam on da su dung he thong. Tam biet!");
                break;
            }

            switch (loggedInUser.getRole()) {
                case EMPLOYEE      -> new EmployeeMenu(loggedInUser).show();
                case SUPPORT_STAFF -> new SupportStaffMenu(loggedInUser).show();
                case ADMIN         -> new AdminMenu(loggedInUser).show();
            }
        }

        JDBCConnection.closeConnection();
    }
}