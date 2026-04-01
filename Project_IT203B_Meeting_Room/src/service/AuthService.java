package service;

import dao.impl.UsersDao;
import model.Role;
import model.User;
import util.PasswordHash;
import util.Validate;

import java.sql.SQLException;

public class AuthService {
    private final UsersDao userDAO = new UsersDao();

    // Đăng nhập
    public User login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null) return null;
        if (!PasswordHash.verify(password, user.getPassword())) return null;
        return user;
    }

    // Đăng kí (chỉ dành cho EMPLOYEE)
    public String register(String username, String password, String fullName,
                           String email, String phone, String department) throws SQLException {
        // Kiểm tra bắt buộc
        if (username.isBlank())  return "Tên đăng nhập không được để trống.";
        if (fullName.isBlank())  return "Họ tên không được để trống.";

        // Kiểm tra định dạng
        if (!Validate.isValidPassword(password))
            return "Mật khẩu phải có tối thiểu 6 ký tự.";
        if (!Validate.isValidEmail(email))
            return "Địa chỉ email không hợp lệ.";
        if (!Validate.isValidPhone(phone))
            return "Số điện thoại không hợp lệ (VD: 0901234567).";

        // Kiểm tra trùng username
        if (userDAO.existsUsername(username))
            return "Tên đăng nhập '" + username + "' đã tồn tại, vui lòng chọn tên khác.";

        // Tạo tài khoản
        User newUser = new User(username, PasswordHash.hash(password), fullName, email, phone, department, Role.EMPLOYEE);
        userDAO.insert(newUser);
        return null; // thành công
    }

    // TẠO tài khoản
    public String createAccount(String username, String password, String fullName,
                                String email, String phone, String department,
                                Role role) throws SQLException {
        if (username.isBlank())  return "Tên đăng nhập không được để trống.";
        if (fullName.isBlank())  return "Họ tên không được để trống.";
        if (!Validate.isValidPassword(password))
            return "Mật khẩu phải có tối thiểu 6 ký tự.";
        if (!Validate.isValidEmail(email))
            return "Email không hợp lệ.";
        if (!Validate.isValidPhone(phone))
            return "Số điện thoại không hợp lệ.";
        if (userDAO.existsUsername(username))
            return "Tên đăng nhập '" + username + "' đã tồn tại.";

        User newUser = new User(username, PasswordHash.hash(password), fullName, email, phone, department, role);
        userDAO.insert(newUser);
        return null;
    }

    // Cập nhật hồ sơ
    public String updateProfile(User user, String fullName, String email,
                                String phone, String department) throws SQLException {
        if (fullName.isBlank()) return "Họ tên không được để trống.";
        if (!Validate.isValidEmail(email))    return "Email không hợp lệ.";
        if (!Validate.isValidPhone(phone))    return "Số điện thoại không hợp lệ.";

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setDepartment(department);
        userDAO.update(user);
        return null;
    }

    // Đổi mật khẩu
    public String changePassword(User user, String oldPwd, String newPwd) throws SQLException {
        if (!PasswordHash.verify(oldPwd, user.getPassword()))
            return "Mật khẩu cũ không đúng.";
        if (!Validate.isValidPassword(newPwd))
            return "Mật khẩu mới phải có tối thiểu 6 ký tự.";

        userDAO.updatePassword(user.getId(), PasswordHash.hash(newPwd));
        user.setPassword(PasswordHash.hash(newPwd));
        return null;
    }
}
