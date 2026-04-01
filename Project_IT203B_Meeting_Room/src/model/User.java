package model;

import java.time.LocalDateTime;

public class User {
    private int           id;
    private String        username;
    private String        password;
    private String        fullName;
    private String        email;
    private String        phone;
    private String        department;
    private Role          role;
    private boolean       active;
    private LocalDateTime createdAt;

    public User(String username, String hash, String fullName, String email, String phone, String department, Role employee) {
        this.username = this.username;
        this.password = password;
        this.fullName = this.fullName;
        this.email = this.email;
        this.phone = this.phone;
        this.department = this.department;
        this.role = role;
        this.active = true;
    }

    public User() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s", id, fullName, username, role.getDisplayName());
    }
}
