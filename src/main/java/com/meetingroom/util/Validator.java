package com.meetingroom.util;

import java.time.LocalDateTime;

/**
 * Kiểm tra hợp lệ dữ liệu đầu vào.
 */
public class Validator {

    /** Email hợp lệ (đơn giản). */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return true; // email là optional
        return email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /** Số điện thoại Việt Nam (10 chữ số). */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return true; // optional
        return phone.matches("^(0[3|5|7|8|9])\\d{8}$");
    }

    /** Mật khẩu tối thiểu 6 ký tự. */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /** Thời gian bắt đầu phải sau hiện tại. */
    public static boolean isFutureTime(LocalDateTime start) {
        return start != null && start.isAfter(LocalDateTime.now());
    }

    /** Thời gian kết thúc phải sau bắt đầu. */
    public static boolean isEndAfterStart(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && end.isAfter(start);
    }
}
