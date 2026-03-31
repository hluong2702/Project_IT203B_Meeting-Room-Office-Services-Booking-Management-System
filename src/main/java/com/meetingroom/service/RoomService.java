package com.meetingroom.service;

import com.meetingroom.dao.RoomDAO;
import com.meetingroom.model.Room;

import java.sql.SQLException;
import java.util.List;

/** Service xử lý nghiệp vụ phòng họp. */
public class RoomService {

    private final RoomDAO roomDAO = new RoomDAO();

    public String addRoom(String name, int capacity, String location, String fixedEquipment)
            throws SQLException {
        if (name.isBlank())     return "Tên phòng không được để trống.";
        if (capacity <= 0)      return "Sức chứa phải lớn hơn 0.";
        if (roomDAO.existsName(name)) return "Tên phòng '" + name + "' đã tồn tại.";

        roomDAO.insert(new Room(name, capacity, location, fixedEquipment));
        return null;
    }

    public String updateRoom(int id, String name, int capacity, String location,
                             String fixedEquipment) throws SQLException {
        if (name.isBlank())  return "Tên phòng không được để trống.";
        if (capacity <= 0)   return "Sức chứa phải lớn hơn 0.";

        Room existing = roomDAO.findById(id);
        if (existing == null) return "Không tìm thấy phòng với ID = " + id;
        if (roomDAO.existsNameExcludeId(name, id))
            return "Tên phòng '" + name + "' đã tồn tại.";

        existing.setName(name);
        existing.setCapacity(capacity);
        existing.setLocation(location);
        existing.setFixedEquipment(fixedEquipment);
        roomDAO.update(existing);
        return null;
    }

    public String deleteRoom(int id) throws SQLException {
        if (roomDAO.findById(id) == null) return "Không tìm thấy phòng với ID = " + id;
        if (roomDAO.hasActiveBooking(id))
            return "Không thể xóa phòng đang có lịch đặt (chưa hủy/từ chối).";
        roomDAO.delete(id);
        return null;
    }

    public Room findById(int id) throws SQLException {
        return roomDAO.findById(id);
    }

    public List<Room> getAllRooms() throws SQLException {
        return roomDAO.findAll();
    }

    public List<Room> searchRooms(String keyword) throws SQLException {
        return roomDAO.searchByName(keyword);
    }
}
