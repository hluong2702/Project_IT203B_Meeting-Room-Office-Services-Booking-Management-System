package com.meetingroom.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Lịch đặt phòng họp. */
public class Booking {

    // ── Trạng thái duyệt ──
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

    // ── Getters / Setters ──
    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }
    public int    getUserId()                    { return userId; }
    public void   setUserId(int userId)          { this.userId = userId; }
    public String getUserFullName()              { return userFullName; }
    public void   setUserFullName(String n)      { this.userFullName = n; }
    public int    getRoomId()                    { return roomId; }
    public void   setRoomId(int roomId)          { this.roomId = roomId; }
    public String getRoomName()                  { return roomName; }
    public void   setRoomName(String n)          { this.roomName = n; }
    public String getTitle()                     { return title; }
    public void   setTitle(String title)         { this.title = title; }
    public LocalDateTime getStartTime()          { return startTime; }
    public void   setStartTime(LocalDateTime t)  { this.startTime = t; }
    public LocalDateTime getEndTime()            { return endTime; }
    public void   setEndTime(LocalDateTime t)    { this.endTime = t; }
    public int    getAttendeesCount()            { return attendeesCount; }
    public void   setAttendeesCount(int c)       { this.attendeesCount = c; }
    public Status getStatus()                    { return status; }
    public void   setStatus(Status s)            { this.status = s; }
    public PrepStatus getPreparationStatus()              { return preparationStatus; }
    public void   setPreparationStatus(PrepStatus ps)     { this.preparationStatus = ps; }
    public Integer getAssignedStaffId()          { return assignedStaffId; }
    public void   setAssignedStaffId(Integer id) { this.assignedStaffId = id; }
    public String getAssignedStaffName()         { return assignedStaffName; }
    public void   setAssignedStaffName(String n) { this.assignedStaffName = n; }
    public String getRejectReason()              { return rejectReason; }
    public void   setRejectReason(String r)      { this.rejectReason = r; }
    public String getNote()                      { return note; }
    public void   setNote(String note)           { this.note = note; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void   setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    public List<BookingEquipment> getEquipmentList() { return equipmentList; }
    public List<BookingService>   getServiceList()   { return serviceList; }

    /** Tính tổng chi phí dịch vụ. */
    public double getTotalServiceCost() {
        return serviceList.stream().mapToDouble(s -> s.unitPrice * s.quantity).sum();
    }
}
