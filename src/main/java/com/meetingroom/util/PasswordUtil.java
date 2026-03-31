package com.meetingroom.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích mã hóa và kiểm tra mật khẩu bằng BCrypt.
 */
public class PasswordUtil {

    /** Mã hóa mật khẩu thô thành BCrypt hash. */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /** Kiểm tra mật khẩu thô có khớp với hash không. */
    public static boolean verify(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
