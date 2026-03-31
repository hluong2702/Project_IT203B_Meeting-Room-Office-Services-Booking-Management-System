package com.meetingroom.service;

import com.meetingroom.dao.UserDAO;
import com.meetingroom.model.Role;
import com.meetingroom.model.User;
import com.meetingroom.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;


/** Service quản lý người dùng (chỉ Admin sử dụng). */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public List<User> getUsersByRole(Role role) throws SQLException {
        return userDAO.findByRole(role);
    }

    public User findById(int id) throws SQLException {
        return userDAO.findById(id);
    }
    // Cần thêm các phương thức:
    public boolean createUser(User user) throws SQLException {
        // Kiểm tra username đã tồn tại chưa
        if (userDAO.existsUsername(user.getUsername())) {
            return false;
        }
        return userDAO.insert(user);
    }

    public boolean updateUser(User user) throws SQLException {
        return userDAO.update(user);
    }

    public boolean updatePassword(int userId, String plainPassword) throws SQLException {
        String hashedPassword = PasswordUtil.hash(plainPassword);
        return userDAO.updatePassword(userId, hashedPassword);
    }

    public boolean existsUsername(String username) throws SQLException {
        return userDAO.existsUsername(username);
    }
}
