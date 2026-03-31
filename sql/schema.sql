-- ============================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT PHÒNG HỌP & DỊCH VỤ VĂN PHÒNG
-- Database Schema (Cập nhật đồng bộ với Java Code)
-- ============================================================
Drop database meeting_room_db;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS meeting_room_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE meeting_room_db;

-- 1. BẢNG NGƯỜI DÙNG (users)
DROP TABLE IF EXISTS users;
CREATE TABLE users (
                       id          INT AUTO_INCREMENT PRIMARY KEY,
                       username    VARCHAR(50)  NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,          -- Hashed password (BCrypt)
                       full_name   VARCHAR(100) NOT NULL,
                       email       VARCHAR(100),
                       phone       VARCHAR(20),
                       department  VARCHAR(100),
                       role        ENUM('EMPLOYEE', 'SUPPORT_STAFF', 'ADMIN') NOT NULL DEFAULT 'EMPLOYEE',
                       active      TINYINT(1)   NOT NULL DEFAULT 1,
                       created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
                       INDEX idx_username (username),
                       INDEX idx_role (role)
) ENGINE=InnoDB;

-- 2. BẢNG PHÒNG HỌP (rooms)
DROP TABLE IF EXISTS rooms;
CREATE TABLE rooms (
                       id              INT AUTO_INCREMENT PRIMARY KEY,
                       name            VARCHAR(100) NOT NULL UNIQUE,
                       capacity        INT          NOT NULL,
                       location        VARCHAR(200),
                       fixed_equipment TEXT,                        -- Mô tả thiết bị cố định
                       active          TINYINT(1)   NOT NULL DEFAULT 1,
                       created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
                       INDEX idx_room_name (name)
) ENGINE=InnoDB;

-- 3. BẢNG THIẾT BỊ DI ĐỘNG (equipment)
DROP TABLE IF EXISTS equipment;
CREATE TABLE equipment (
                           id              INT AUTO_INCREMENT PRIMARY KEY,
                           name            VARCHAR(100) NOT NULL UNIQUE,
                           total_quantity  INT          NOT NULL DEFAULT 1,
                           available_qty   INT          NOT NULL DEFAULT 1,
                           status          ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
                           created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. BẢNG DỊCH VỤ ĐI KÈM (services)
DROP TABLE IF EXISTS services;
CREATE TABLE services (
                          id          INT AUTO_INCREMENT PRIMARY KEY,
                          name        VARCHAR(100) NOT NULL UNIQUE,
                          unit_price  DECIMAL(10, 2) NOT NULL DEFAULT 0,
                          unit        VARCHAR(50),                     -- chai, phần, lần...
                          active      TINYINT(1)   NOT NULL DEFAULT 1,
                          created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 5. BẢNG ĐẶT PHÒNG (bookings)
DROP TABLE IF EXISTS bookings;
CREATE TABLE bookings (
                          id                  INT AUTO_INCREMENT PRIMARY KEY,
                          user_id             INT          NOT NULL,
                          room_id             INT          NOT NULL,
                          title               VARCHAR(200) NOT NULL,
                          start_time          DATETIME     NOT NULL,
                          end_time            DATETIME     NOT NULL,
                          attendees_count     INT          NOT NULL DEFAULT 1,
                          status              ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
                          preparation_status  ENUM('NOT_STARTED', 'PREPARING', 'READY', 'MISSING_EQUIPMENT') DEFAULT 'NOT_STARTED',
                          assigned_staff_id   INT,
                          reject_reason       VARCHAR(500),
                          note                TEXT,
                          created_at          DATETIME     DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_booking_user  FOREIGN KEY (user_id)           REFERENCES users(id) ON DELETE RESTRICT,
                          CONSTRAINT fk_booking_room  FOREIGN KEY (room_id)           REFERENCES rooms(id) ON DELETE RESTRICT,
                          CONSTRAINT fk_booking_staff FOREIGN KEY (assigned_staff_id) REFERENCES users(id) ON DELETE SET NULL,

                          INDEX idx_time_range (start_time, end_time),
                          INDEX idx_booking_status (status)
) ENGINE=InnoDB;

-- 6. BẢNG THIẾT BỊ ĐƯỢC ĐẶT KÈM (booking_equipment)
DROP TABLE IF EXISTS booking_equipment;
CREATE TABLE booking_equipment (
                                   id              INT AUTO_INCREMENT PRIMARY KEY,
                                   booking_id      INT NOT NULL,
                                   equipment_id    INT NOT NULL,
                                   quantity        INT NOT NULL DEFAULT 1,

                                   CONSTRAINT fk_be_booking   FOREIGN KEY (booking_id)   REFERENCES bookings(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_be_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 7. BẢNG DỊCH VỤ ĐƯỢC ĐẶT KÈM (booking_services)
DROP TABLE IF EXISTS booking_services;
CREATE TABLE booking_services (
                                  id          INT AUTO_INCREMENT PRIMARY KEY,
                                  booking_id  INT NOT NULL,
                                  service_id  INT NOT NULL,
                                  quantity    INT NOT NULL DEFAULT 1,

                                  CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_bs_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- DỮ LIỆU MẪU (SEED DATA)
-- ============================================================

-- Users: mật khẩu đã hash BCrypt
-- Admin: admin / Admin@123
INSERT INTO users (username, password, full_name, email, phone, department, role) VALUES
    ('admin1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhl', 'Quản trị viên', 'admin@company.com', '0900000001', 'IT', 'ADMIN');

-- Support Staff: support1 / Support@123
INSERT INTO users (username, password, full_name, email, phone, department, role) VALUES
    ('support1', '$2a$10$TtFBvd3f5G6M8A.VT/GZ6uiXjvdFNh.mXcM3G3fD2YAlB0Oa0bQy', 'Nguyễn Hỗ Trợ', 'support1@company.com', '0900000002', 'Hành chính', 'SUPPORT_STAFF');

-- Employee: emp1 / Emp@123
INSERT INTO users (username, password, full_name, email, phone, department, role) VALUES
    ('emp1', '$2a$10$d4e5bXM3rCNPH2A0FVIR1OABqjP5dZqXiMxTm.jQ4vG0c4i7pGUe', 'Trần Văn Nhân', 'emp1@company.com', '0900000003', 'Kỹ thuật', 'EMPLOYEE');

-- Rooms
INSERT INTO rooms (name, capacity, location, fixed_equipment) VALUES
                                                                  ('Phòng họp A101', 10, 'Tầng 1, Tòa A', 'Máy chiếu, Bảng trắng, Điều hòa'),
                                                                  ('Phòng họp B202', 20, 'Tầng 2, Tòa B', 'TV 75 inch, Hệ thống âm thanh, Điều hòa'),
                                                                  ('Phòng hội nghị C301', 50, 'Tầng 3, Tòa C', 'Sân khấu nhỏ, Micro không dây, Máy chiếu lớn');

-- Mobile Equipment
INSERT INTO equipment (name, total_quantity, available_qty, status) VALUES
                                                                        ('Máy chiếu di động', 3, 3, 'ACTIVE'),
                                                                        ('Loa Bluetooth', 5, 5, 'ACTIVE'),
                                                                        ('Bảng tương tác', 2, 2, 'ACTIVE'),
                                                                        ('Webcam HD', 4, 4, 'ACTIVE');

-- Services
INSERT INTO services (name, unit_price, unit) VALUES
                                                  ('Nước suối', 5000, 'chai'),
                                                  ('Trà nóng', 10000, 'phần'),
                                                  ('Bánh ngọt', 25000, 'phần'),
                                                  ('Cà phê', 15000, 'ly'),
                                                  ('Lau dọn phòng', 50000, 'lần');

select *from users;