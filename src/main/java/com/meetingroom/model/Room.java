package com.meetingroom.model;

import java.time.LocalDateTime;

/** Phòng họp. */
public class Room {
    private int           id;
    private String        name;
    private int           capacity;
    private String        location;
    private String        fixedEquipment;
    private boolean       active;
    private LocalDateTime createdAt;

    public Room() {}
    public Room(String name, int capacity, String location, String fixedEquipment) {
        this.name = name; this.capacity = capacity;
        this.location = location; this.fixedEquipment = fixedEquipment;
        this.active = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getFixedEquipment() { return fixedEquipment; }
    public void setFixedEquipment(String fe) { this.fixedEquipment = fe; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return String.format("[%d] %s - Sức chứa: %d - Vị trí: %s", id, name, capacity, location);
    }
}
