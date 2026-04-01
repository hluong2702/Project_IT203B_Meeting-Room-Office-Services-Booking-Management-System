package dao.interfaces;

import model.Equipment;

import java.sql.SQLException;
import java.util.List;

public interface IEquipmentDao {
    boolean insert(Equipment equipment) throws SQLException;

    boolean update(Equipment equipment) throws SQLException;

    boolean delete(int id) throws SQLException;

    Equipment findById(int id) throws SQLException;

    boolean existsName(String name) throws SQLException;

    boolean existsNameExcludeId(String name, int excludeId) throws SQLException;

    List<Equipment> findAll() throws SQLException;

    List<Equipment> findActive() throws SQLException;

    boolean adjustAvailable(int id, int delta) throws SQLException;

    boolean isUsedInBooking(int id) throws SQLException;
}
