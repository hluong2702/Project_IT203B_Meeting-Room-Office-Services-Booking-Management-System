package com.meetingroom.presentation;

import com.meetingroom.model.User;
import com.meetingroom.service.AuthService;
import com.meetingroom.util.ConsoleUtil;

import java.sql.SQLException;

/**
 * Menu xác thực: Đăng nhập / Đăng ký tài khoản Employee.
 */
public class AuthMenu {

    private final AuthService authService = new AuthService();

    /**
     * Hiển thị menu chào, trả về User đã đăng nhập hoặc null nếu thoát.
     */
    public User show() {
        while (true) {
            ConsoleUtil.printHeader("HỆ THỐNG QUẢN LÝ ĐẶT PHÒNG HỌP");
            System.out.println("  1. Đăng nhập");
            System.out.println("  2. Đăng ký tài khoản (Nhân viên)");
            System.out.println("  0. Thoát chương trình");
            ConsoleUtil.printSeparator();
            int choice = ConsoleUtil.readChoice("Chọn: ");

            switch (choice) {
                case 1  -> { User u = login(); if (u != null) return u; }
                case 2  -> register();
                case 0  -> { return null; }
                default -> ConsoleUtil.error("Lựa chọn không hợp lệ.");
            }
        }
    }

    // ──── ĐĂNG NHẬP ────
    private User login() {
        System.out.println();
        ConsoleUtil.printHeader("ĐĂNG NHẬP");
        String username = ConsoleUtil.inputString("Tên đăng nhập: ");
        String password = ConsoleUtil.inputPassword("Mật khẩu      : ");

        try {
            User u = authService.login(username, password);
            if (u == null) {
                ConsoleUtil.error("Tên đăng nhập hoặc mật khẩu không đúng.");
                ConsoleUtil.pause();
                return null;
            }
            ConsoleUtil.success("Đăng nhập thành công! Xin chào, " + u.getFullName() +
                                " (" + u.getRole().getDisplayName() + ")");
            ConsoleUtil.pause();
            return u;
        } catch (SQLException e) {
            ConsoleUtil.error("Lỗi kết nối CSDL: " + e.getMessage());
            ConsoleUtil.pause();
            return null;
        }
    }

    // ──── ĐĂNG KÝ ────
    private void register() {
        System.out.println();
        ConsoleUtil.printHeader("ĐĂNG KÝ TÀI KHOẢN NHÂN VIÊN");
        String username   = ConsoleUtil.inputString("Tên đăng nhập : ");
        String password   = ConsoleUtil.inputPassword("Mật khẩu (≥6) : ");
        String fullName   = ConsoleUtil.inputString("Họ và tên     : ");
        String email      = ConsoleUtil.inputOptional("Email         : ");
        String phone      = ConsoleUtil.inputOptional("Số điện thoại : ");
        String department = ConsoleUtil.inputOptional("Phòng ban     : ");

        try {
            String err = authService.register(username, password, fullName,
                                              email, phone, department);
            if (err != null) {
                ConsoleUtil.error(err);
            } else {
                ConsoleUtil.success("Đăng ký thành công! Bạn có thể đăng nhập ngay.");
            }
        } catch (SQLException e) {
            ConsoleUtil.error("Lỗi CSDL: " + e.getMessage());
        }
        ConsoleUtil.pause();
    }
}
