package dao.interfaces;

import model.Role;
import model.User;

import java.sql.SQLException;
import java.util.List;

public interface IUserDao {
    boolean insert(User user) throws SQLException;

    boolean update(User user) throws SQLException;

    boolean updatePassword(int userId, String hashedPassword) throws SQLException;

    User findById(int id) throws SQLException;

    User findByUsername(String username) throws SQLException;

    boolean existsUsername(String username) throws SQLException;

    List<User> findAll() throws SQLException;

    List<User> findByRole(Role role) throws SQLException;
}
