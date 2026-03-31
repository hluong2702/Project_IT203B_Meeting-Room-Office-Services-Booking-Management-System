package com.meetingroom.model;

/** Vai trò người dùng trong hệ thống. */
public enum Role {
    EMPLOYEE("Nhân viên"),
    SUPPORT_STAFF("Nhân viên hỗ trợ"),
    ADMIN("Quản trị viên");

    private final String displayName;

    Role(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    public static Role from(String s) {
        for (Role r : values()) if (r.name().equalsIgnoreCase(s)) return r;
        return EMPLOYEE;
    }
}
