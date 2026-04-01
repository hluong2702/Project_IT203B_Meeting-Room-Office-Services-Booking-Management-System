package service;

import dao.impl.UsersDao;
import model.User;
import model.Role;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private static final UsersDao usersDao = new UsersDao();

    public List<User> getAllUsers() throws SQLException {
        return usersDao.findAll();
    }

    public List<User> getUsersByRole(Role role) throws SQLException {
        return usersDao.findByRole(role);
    }

    public User findById(int id) throws SQLException {
        return usersDao.findById(id);
    }
}
