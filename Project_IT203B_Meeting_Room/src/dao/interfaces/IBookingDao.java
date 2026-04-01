package dao.interfaces;

import model.Booking;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface IBookingDao {
    int insert(Booking booking) throws SQLException;

    void insertEquipment(int bookingId, int equipmentId, int quantity) throws SQLException;

    void insertService(int bookingId, int serviceId, int quantity) throws SQLException;

    boolean updateStatus(int id, Booking.Status status, String rejectReason) throws SQLException;

    boolean updatePrepStatus(int id, Booking.PrepStatus prepStatus) throws SQLException;

    boolean assignStaff(int bookingId, int staffId) throws SQLException;

    Booking findById(int id) throws SQLException;

    List<Booking> findByUser(int userId) throws SQLException;

    List<Booking> findPending() throws SQLException;

    List<Booking> findByAssignedStaff(int staffId) throws SQLException;

    List<Booking> findAll() throws SQLException;

    boolean hasConflict(int roomId, LocalDateTime start, LocalDateTime end, int excludeBookingId) throws SQLException;

    void loadEquipmentAndServices(Booking booking) throws SQLException;
}
