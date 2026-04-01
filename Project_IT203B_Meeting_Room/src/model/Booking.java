package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking {
    public enum Status { PENDING, APPROVED, REJECTED, CANCELLED }

    // ── Trạng thái chuẩn bị ──
    public enum PrepStatus { NOT_STARTED, PREPARING, READY, MISSING_EQUIPMENT }

    // ── Hạng mục đặt thiết bị / dịch vụ kèm ──
    public static class BookingEquipment {
        public int equipmentId;
        public String equipmentName;
        public int quantity;
        public BookingEquipment(int id, String name, int qty) {
            this.equipmentId = id; this.equipmentName = name; this.quantity = qty;
        }
    }
    public static class BookingService {
        public int serviceId;
        public String serviceName;
        public int quantity;
        public double unitPrice;
        public BookingService(int id, String name, int qty, double price) {
            this.serviceId = id; this.serviceName = name; this.quantity = qty; this.unitPrice = price;
        }
    }

    // ── Fields ──
    private int         id;
    private int         userId;
    private String      userFullName;       // để hiển thị
    private int         roomId;
    private String      roomName;           // để hiển thị
    private String      title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int         attendeesCount;
    private Status      status;
    private PrepStatus  preparationStatus;
    private Integer     assignedStaffId;
    private String      assignedStaffName;
    private String      rejectReason;
    private String      note;
    private LocalDateTime createdAt;

    private List<BookingEquipment> equipmentList = new ArrayList<>();
    private List<BookingService>   serviceList   = new ArrayList<>();

    public Booking() {
        this.status            = Status.PENDING;
        this.preparationStatus = PrepStatus.NOT_STARTED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getAttendeesCount() {
        return attendeesCount;
    }

    public void setAttendeesCount(int attendeesCount) {
        this.attendeesCount = attendeesCount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public PrepStatus getPreparationStatus() {
        return preparationStatus;
    }

    public void setPreparationStatus(PrepStatus preparationStatus) {
        this.preparationStatus = preparationStatus;
    }

    public Integer getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(Integer assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setEquipmentList(List<BookingEquipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public void setServiceList(List<BookingService> serviceList) {
        this.serviceList = serviceList;
    }

    public List<BookingEquipment> getEquipmentList() { return equipmentList; }
    public List<BookingService>   getServiceList()   { return serviceList; }

    /** Tính tổng chi phí dịch vụ. */
    public double getTotalServiceCost() {
        return serviceList.stream().mapToDouble(s -> s.unitPrice * s.quantity).sum();
    }
}
