# Hệ thống Quản lý Đặt phòng họp & Dịch vụ Văn phòng
> **IT203B – Java Advanced (K24)** | Mã dự án: PRJ-MEETING-JAVA-05

---

## 🗂 Cấu trúc dự án

```
meeting-room-system/
├── pom.xml
├── sql/
│   └── schema.sql                  ← Script tạo CSDL + dữ liệu mẫu
└── src/main/java/com/meetingroom/
    ├── Main.java                   ← Điểm khởi chạy
    ├── model/                      ← Thực thể (User, Room, Equipment, Service, Booking)
    ├── dao/                        ← Tương tác CSDL qua JDBC
    ├── service/                    ← Logic nghiệp vụ
    ├── presentation/               ← Giao diện console (menu theo vai trò)
    └── util/                       ← DBConnection, PasswordUtil, ConsoleUtil, Validator
```

---

## ⚙️ Yêu cầu hệ thống

| Công cụ | Phiên bản |
|---------|-----------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |

---

## 🚀 Hướng dẫn cài đặt & chạy

### Bước 1 – Chuẩn bị CSDL

```sql
-- Chạy file sql/schema.sql trong MySQL Workbench hoặc dòng lệnh:
mysql -u root -p < sql/schema.sql
```

### Bước 2 – Cấu hình kết nối

Mở file `src/main/java/com/meetingroom/util/DBConnection.java` và chỉnh:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/meeting_room_db?...";
private static final String USERNAME = "root";    // ← tên user MySQL của bạn
private static final String PASSWORD = "";        // ← mật khẩu MySQL của bạn
```

### Bước 3 – Build & chạy

```bash
# Build
mvn clean package -q

# Chạy
java -jar target/meeting-room-system-jar-with-dependencies.jar
```

Hoặc chạy thẳng từ IDE: chạy class `com.meetingroom.Main`.

---

## 👤 Tài khoản mẫu

| Vai trò | Username | Mật khẩu |
|---------|----------|----------|
| Admin | `admin` | `Admin@123` |
| Nhân viên hỗ trợ | `support1` | `Support@123` |
| Nhân viên | `emp1` | `Emp@123` |

> Mật khẩu được mã hóa BCrypt trong CSDL.

---

## 📋 Tính năng đã hoàn thiện

### Bắt buộc (60 điểm)

| # | Phân hệ | Tính năng |
|---|---------|-----------|
| 1 | Tài khoản & Xác thực | Đăng ký, đăng nhập, phân quyền 3 vai trò, mã hóa mật khẩu BCrypt |
| 2 | Quản lý Phòng họp | CRUD đầy đủ, tìm kiếm tương đối theo tên, kiểm tra ràng buộc xóa |
| 3 | Quản lý Thiết bị | CRUD, theo dõi số lượng khả dụng, kiểm tra khi đặt |
| 4 | Quản lý Dịch vụ | CRUD, đơn giá, đơn vị tính |
| 5 | Đặt phòng | Chọn thiết bị + dịch vụ, kiểm tra xung đột lịch, sức chứa, thời gian |
| 6 | Duyệt & Phân công | Admin duyệt/từ chối, phân công support staff |
| 7 | Cập nhật chuẩn bị | Support staff cập nhật trạng thái: Preparing / Ready / Thiếu thiết bị |
| 8 | Xem lịch | Employee xem lịch riêng + trạng thái chuẩn bị |

### Phi chức năng
- ✅ Validation đầy đủ tất cả ô nhập liệu
- ✅ Bắt ngoại lệ, không crash đột ngột
- ✅ Phân quyền truy cập theo vai trò
- ✅ Hiển thị bảng dữ liệu đẹp, có màu ANSI
- ✅ Kiến trúc 4 tầng rõ ràng: Model – DAO – Service – Presentation

---

## 🏗 Kiến trúc hệ thống

```
┌─────────────────────────────────────────────┐
│           PRESENTATION (Console)            │
│  AuthMenu | EmployeeMenu | AdminMenu | ...  │
├─────────────────────────────────────────────┤
│               SERVICE LAYER                 │
│  AuthService | BookingService | RoomService │
├─────────────────────────────────────────────┤
│                 DAO LAYER                   │
│  UserDAO | BookingDAO | RoomDAO | ...       │
├─────────────────────────────────────────────┤
│              MODEL LAYER                    │
│  User | Room | Equipment | Booking | ...    │
└─────────────────────────────────────────────┘
               ↕ JDBC
┌─────────────────────────────────────────────┐
│              MySQL Database                 │
│  meeting_room_db                            │
└─────────────────────────────────────────────┘
```

---

## 🗄 Sơ đồ CSDL

```
users ──────────────────────────────┐
  id, username, password(BCrypt),   │
  full_name, email, phone,          │
  department, role, active          │
                                    │
rooms ──────────────────┐          │
  id, name, capacity,   │          │
  location,             │          │
  fixed_equipment       │          │
                        │          │
bookings ───────────────┘          │
  id, user_id(FK) ─────────────────┘
  room_id(FK)
  title, start_time, end_time,
  attendees_count, status,
  preparation_status,
  assigned_staff_id(FK→users)
        │
        ├── booking_equipment ── equipment
        │     booking_id, equipment_id, qty
        │
        └── booking_services ── services
              booking_id, service_id, qty
```
