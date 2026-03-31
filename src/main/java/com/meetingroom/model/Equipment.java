package com.meetingroom.model;

import java.time.LocalDateTime;

/** Thiết bị di động dùng chung. */
public class Equipment {
    private int           id;
    private String        name;
    private int           totalQuantity;
    private int           availableQty;
    private String        status;   // ACTIVE / INACTIVE / MAINTENANCE
    private LocalDateTime createdAt;

    public Equipment() {}
    public Equipment(String name, int totalQuantity) {
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.availableQty  = totalQuantity;
        this.status = "ACTIVE";
    }

    public int    getId()                          { return id; }
    public void   setId(int id)                    { this.id = id; }
    public String getName()                        { return name; }
    public void   setName(String name)             { this.name = name; }
    public int    getTotalQuantity()               { return totalQuantity; }
    public void   setTotalQuantity(int q)          { this.totalQuantity = q; }
    public int    getAvailableQty()                { return availableQty; }
    public void   setAvailableQty(int q)           { this.availableQty = q; }
    public String getStatus()                      { return status; }
    public void   setStatus(String status)         { this.status = status; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }

    @Override public String toString() {
        return String.format("[%d] %s - Tổng: %d - Khả dụng: %d - Trạng thái: %s",
                id, name, totalQuantity, availableQty, status);
    }
}
