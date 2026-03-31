package com.meetingroom;

import com.meetingroom.model.User;
import com.meetingroom.presentation.AdminMenu;
import com.meetingroom.presentation.AuthMenu;
import com.meetingroom.presentation.EmployeeMenu;
import com.meetingroom.presentation.SupportStaffMenu;
import com.meetingroom.util.ConsoleUtil;
import com.meetingroom.util.DBConnection;

public class Main {

    public static void main(String[] args) {

        // Kiểm tra kết nối CSDL ngay khi khởi động
        try {
            DBConnection.getConnection();
            ConsoleUtil.success("Kết nối cơ sở dữ liệu thành công!");
        } catch (Exception e) {
            ConsoleUtil.error("Không thể kết nối CSDL: " + e.getMessage());
            ConsoleUtil.error("Vui lòng kiểm tra cấu hình trong DBConnection.java và đảm bảo MySQL đang chạy.");
            return;
        }

        // Vòng lặp chính: đăng nhập → dùng hệ thống → đăng xuất → quay lại hoặc thoát
        AuthMenu authMenu = new AuthMenu();
        while (true) {
            User loggedInUser = authMenu.show();

            if (loggedInUser == null) {
                // Người dùng chọn "Thoát"
                ConsoleUtil.info("Cảm ơn đã sử dụng hệ thống. Tạm biệt!");
                break;
            }

            // Phân hướng theo vai trò
            switch (loggedInUser.getRole()) {
                case EMPLOYEE      -> new EmployeeMenu(loggedInUser).show();
                case SUPPORT_STAFF -> new SupportStaffMenu(loggedInUser).show();
                case ADMIN         -> new AdminMenu(loggedInUser).show();
            }
        }

        DBConnection.closeConnection();
    }

}
