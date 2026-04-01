package model;

import java.time.LocalDateTime;

public class Service {

    private int           id;
    private String        name;
    private double        unitPrice;
    private String        unit;
    private boolean       active;
    private LocalDateTime createdAt;

    public Service() {}

    public Service(String name, double unitPrice, String unit) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.active = true;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override public String toString() {
        return String.format("[%d] %s - %.0f đ/%s", id, name, unitPrice, unit);
    }
}
