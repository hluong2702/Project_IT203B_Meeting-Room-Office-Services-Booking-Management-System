package dao.interfaces;

import model.Service;

import java.sql.SQLException;
import java.util.List;

public interface IServiceDao {
    boolean insert(Service service) throws SQLException;

    boolean update(Service service) throws SQLException;

    boolean delete(int id) throws SQLException;

    Service findById(int id) throws SQLException;

    boolean existsName(String name) throws SQLException;

    boolean existsNameExcludeId(String name, int excludeId) throws SQLException;

    List<Service> findAll() throws SQLException;
}
