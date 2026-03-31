package com.meetingroom.service;

import com.meetingroom.dao.UserDAO;
import com.meetingroom.model.Role;
import com.meetingroom.model.User;
import com.meetingroom.util.PasswordUtil;
import com.meetingroom.util.Validator;

import java.sql.SQLException;

/**
 * Service xử lý xác thực và quản lý tài khoản.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // ──── ĐĂNG NHẬP ────
    /**
     * Kiểm tra username/password và trả về User nếu hợp lệ.
     * @return User nếu đăng nhập thành công, null nếu sai.
     */
    public User login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null) return null;
        if (!PasswordUtil.verify(password, user.getPassword())) return null;
        return user;
    }

    // ──── ĐĂNG KÝ (chỉ dành cho EMPLOYEE) ────
    /**
     * @return null nếu thành công, chuỗi thông báo lỗi nếu thất bại.
     */
    public String register(String username, String password, String fullName,
                           String email, String phone, String department) throws SQLException {
        // Kiểm tra bắt buộc
        if (username.isBlank())  return "Tên đăng nhập không được để trống.";
        if (fullName.isBlank())  return "Họ tên không được để trống.";

        // Kiểm tra định dạng
        if (!Validator.isValidPassword(password))
            return "Mật khẩu phải có tối thiểu 6 ký tự.";
        if (!Validator.isValidEmail(email))
            return "Địa chỉ email không hợp lệ.";
        if (!Validator.isValidPhone(phone))
            return "Số điện thoại không hợp lệ (VD: 0901234567).";

        // Kiểm tra trùng username
        if (userDAO.existsUsername(username))
            return "Tên đăng nhập '" + username + "' đã tồn tại, vui lòng chọn tên khác.";

        // Tạo tài khoản
        User newUser = new User(username, PasswordUtil.hash(password),
                fullName, email, phone, department, Role.EMPLOYEE);
        userDAO.insert(newUser);
        return null; // thành công
    }

    // ──── TẠO TÀI KHOẢN BỞI ADMIN ────
    public String createAccount(String username, String password, String fullName,
                                String email, String phone, String department,
                                Role role) throws SQLException {
        if (username.isBlank())  return "Tên đăng nhập không được để trống.";
        if (fullName.isBlank())  return "Họ tên không được để trống.";
        if (!Validator.isValidPassword(password))
            return "Mật khẩu phải có tối thiểu 6 ký tự.";
        if (!Validator.isValidEmail(email))
            return "Email không hợp lệ.";
        if (!Validator.isValidPhone(phone))
            return "Số điện thoại không hợp lệ.";
        if (userDAO.existsUsername(username))
            return "Tên đăng nhập '" + username + "' đã tồn tại.";

        User newUser = new User(username, PasswordUtil.hash(password),
                fullName, email, phone, department, role);
        userDAO.insert(newUser);
        return null;
    }

    // ──── CẬP NHẬT HỒ SƠ ────
    public String updateProfile(User user, String fullName, String email,
                                String phone, String department) throws SQLException {
        if (fullName.isBlank()) return "Họ tên không được để trống.";
        if (!Validator.isValidEmail(email))    return "Email không hợp lệ.";
        if (!Validator.isValidPhone(phone))    return "Số điện thoại không hợp lệ.";

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setDepartment(department);
        userDAO.update(user);
        return null;
    }

    // ──── ĐỔI MẬT KHẨU ────
    public String changePassword(User user, String oldPwd, String newPwd) throws SQLException {
        if (!PasswordUtil.verify(oldPwd, user.getPassword()))
            return "Mật khẩu cũ không đúng.";
        if (!Validator.isValidPassword(newPwd))
            return "Mật khẩu mới phải có tối thiểu 6 ký tự.";

        userDAO.updatePassword(user.getId(), PasswordUtil.hash(newPwd));
        user.setPassword(PasswordUtil.hash(newPwd));
        return null;
    }
}
