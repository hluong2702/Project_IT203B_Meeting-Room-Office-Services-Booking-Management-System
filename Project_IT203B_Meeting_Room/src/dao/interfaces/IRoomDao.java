package dao.interfaces;

import model.Room;

import java.sql.SQLException;
import java.util.List;

public interface IRoomDao {
    boolean insert(Room room) throws SQLException;

    boolean update(Room room) throws SQLException;

    boolean delete(int id) throws SQLException;

    Room findById(int id) throws SQLException;

    boolean existsName(String name) throws SQLException;

    boolean existsNameExcludeId(String name, int excludeId) throws SQLException;

    List<Room> findAll() throws SQLException;

    List<Room> searchByName(String keyword) throws SQLException;

    boolean hasActiveBooking(int roomId) throws SQLException;
}
