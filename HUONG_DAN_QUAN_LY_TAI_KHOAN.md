# HƯỚNG DẪN THÊM CHỨC NĂNG QUẢN LÝ TÀI KHOẢN

## ✅ Đã hoàn thành

Đã thêm 2 chức năng mới vào màn hình **Quản Lý Người Dùng**:
1. **Tạo mới tài khoản** - Admin có thể tạo tài khoản người dùng mới
2. **Khóa/Mở khóa tài khoản** - Quản lý trạng thái hoạt động của tài khoản

---

## 📋 BƯỚC 1: Cập nhật Database

### Chạy file SQL để thêm cột `status`:

```sql
-- File: Server/add_account_status.sql
ALTER TABLE user ADD COLUMN IF NOT EXISTS status INT DEFAULT 1 COMMENT '1=Active, 0=Locked';
UPDATE user SET status = 1 WHERE status IS NULL;
CREATE INDEX IF NOT EXISTS idx_user_status ON user(status);
```

**Cách chạy:**
1. Mở phpMyAdmin
2. Chọn database `appbandienthoai`
3. Vào tab "SQL"
4. Copy nội dung file `Server/add_account_status.sql` và chạy
5. Hoặc chạy trực tiếp: `ALTER TABLE user ADD COLUMN status INT DEFAULT 1;`

---

## 🎯 TÍNH NĂNG MỚI

### 1. Tạo Tài Khoản Mới

**Cách sử dụng:**
- Vào màn hình **Quản Lý Người Dùng**
- Click nút **FAB (Floating Action Button)** màu xanh lá ở góc dưới phải (biểu tượng dấu +)
- Điền đầy đủ thông tin:
  - Email (phải hợp lệ)
  - Tên người dùng
  - Mật khẩu (tối thiểu 6 ký tự)
  - Số điện thoại (10 chữ số)
  - Tick vào "Cấp quyền Admin" nếu muốn tạo tài khoản admin
- Click "Tạo"

**Validation:**
- ✅ Email phải đúng định dạng và chưa tồn tại
- ✅ Mật khẩu tối thiểu 6 ký tự
- ✅ Số điện thoại phải có đúng 10 chữ số
- ✅ Tất cả trường đều bắt buộc

**Kết quả:**
- Tài khoản mới được tạo với trạng thái **Active** (hoạt động)
- Hiển thị ngay trong danh sách người dùng
- Người dùng mới có thể đăng nhập ngay lập tức

---

### 2. Khóa/Mở Khóa Tài Khoản

**Cách sử dụng:**
- Trong danh sách người dùng
- Mỗi item có Switch "Trạng thái tài khoản"
- **Switch BẬT (ON)** = Tài khoản đang hoạt động ✅
- **Switch TẮT (OFF)** = Tài khoản bị khóa 🔒
- Toggle switch để khóa/mở khóa tài khoản
- Xác nhận trong dialog popup

**Quy tắc:**
- ❌ Không thể khóa chính mình
- ❌ Không thể xóa chính mình
- ✅ Có thể khóa/mở khóa bất kỳ tài khoản nào khác
- ✅ Tài khoản bị khóa không thể đăng nhập

**Badge hiển thị:**
- Badge "🔒 KHÓA" màu đỏ hiển thị trên tài khoản bị khóa
- Badge "ADMIN" màu cam hiển thị trên tài khoản admin

---

## 🔒 BẢO MẬT

### Khi tài khoản bị khóa:
1. Người dùng **KHÔNG THỂ đăng nhập**
2. Hiển thị thông báo: *"🔒 Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!"*
3. Dữ liệu tài khoản vẫn được giữ nguyên
4. Admin có thể mở khóa bất kỳ lúc nào

### Bảo vệ Admin:
- Admin không thể khóa hoặc xóa chính mình
- Nếu cố gắng, hiển thị cảnh báo và reset lại switch

---

## 🎨 GIAO DIỆN

### Danh sách người dùng:
```
┌──────────────────────────────────────┐
│ Nguyễn Văn A         [ADMIN]         │
│ 📧 admin@example.com                 │
│ 📱 0123456789                        │
│ Loại: Thường                         │
│ Vai trò: Admin                       │
│ ─────────────────────────────────    │
│ Trạng thái tài khoản:  [ON]      🗑️ │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│ Trần Thị B           🔒 KHÓA         │
│ 📧 user@example.com                  │
│ 📱 0987654321                        │
│ Loại: Google                         │
│ Vai trò: User                        │
│ ─────────────────────────────────    │
│ Trạng thái tài khoản:  [OFF]     🗑️ │
└──────────────────────────────────────┘
```

### Dialog tạo tài khoản:
```
╔══════════════════════════════╗
║   Tạo Tài Khoản Mới          ║
╠══════════════════════════════╣
║ Email: _____________________ ║
║ Tên: _______________________ ║
║ Mật khẩu: __________________ ║
║ SĐT: _______________________ ║
║ ☐ Cấp quyền Admin            ║
║ ⚠️ Tài khoản sẽ được tạo với ║
║    trạng thái hoạt động      ║
╠══════════════════════════════╣
║         [Hủy]    [Tạo]       ║
╚═══════════════════════════��══╝
```

---

## 📁 CÁC FILE ĐÃ THAY ĐỔI

### Backend (PHP):
1. ✅ `Server/add_account_status.sql` - SQL thêm cột status
2. ✅ `Server/createUser.php` - API tạo tài khoản mới
3. ✅ `Server/updateUserStatus.php` - API cập nhật trạng thái tài khoản
4. ✅ `Server/dangnhap.php` - Thêm kiểm tra trạng thái khi đăng nhập

### Android (Java):
1. ✅ `User.java` - Thêm trường status + methods
2. ✅ `ApiBanHang.java` - Thêm 2 API mới
3. ✅ `NguoiDungAdapter.java` - Thêm Switch và badge khóa
4. ✅ `QuanLyNguoiDungActivity.java` - Logic tạo mới & khóa/mở tài khoản
5. ✅ `item_nguoi_dung.xml` - Layout item người dùng
6. ✅ `dialog_create_user.xml` - Layout dialog tạo tài khoản
7. ✅ `activity_quan_ly_nguoi_dung.xml` - Thêm FAB button

---

## 🧪 CÁCH TEST

### Test tạo tài khoản:
1. Đăng nhập với tài khoản admin
2. Vào "Quản Lý Người Dùng"
3. Click nút FAB (+) màu xanh
4. Điền thông tin:
   - Email: `testuser@gmail.com`
   - Username: `Test User`
   - Password: `123456`
   - Mobile: `0912345678`
   - Không tick Admin
5. Click "Tạo"
6. ✅ Kiểm tra tài khoản mới xuất hiện trong danh sách
7. ✅ Thử đăng nhập với tài khoản mới

### Test khóa tài khoản:
1. Trong danh sách, tìm tài khoản vừa tạo
2. Tắt Switch "Trạng thái tài khoản"
3. Xác nhận khóa tài khoản
4. ✅ Badge "🔒 KHÓA" xuất hiện
5. Đăng xuất và thử đăng nhập bằng tài khoản bị khóa
6. ✅ Phải hiển thị thông báo "Tài khoản đã bị khóa"

### Test mở khóa tài khoản:
1. Đăng nhập lại bằng admin
2. Vào "Quản Lý Người Dùng"
3. Tìm tài khoản bị khóa
4. Bật Switch "Trạng thái tài khoản"
5. Xác nhận mở khóa
6. ✅ Badge "🔒 KHÓA" biến mất
7. ✅ Tài khoản có thể đăng nhập lại

### Test validation:
1. Click FAB để tạo tài khoản
2. Thử bỏ trống từng trường → ✅ Hiển thị lỗi
3. Thử nhập email không hợp lệ → ✅ Hiển thị lỗi
4. Thử nhập password < 6 ký tự → ✅ Hiển thị lỗi
5. Thử nhập SĐT không phải 10 số → ✅ Hiển thị lỗi
6. Thử tạo email đã tồn tại → ✅ Hiển thị lỗi

### Test bảo vệ admin:
1. Đăng nhập với admin
2. Vào "Quản Lý Người Dùng"
3. Tìm chính tài khoản admin đang đăng nhập
4. Thử tắt Switch → ✅ Hiển thị cảnh báo, switch reset lại
5. Thử click nút xóa → ✅ Hiển thị cảnh báo

---

## 🔧 TROUBLESHOOTING

### Lỗi: "Unknown column 'status'"
**Nguyên nhân:** Chưa chạy SQL thêm cột status
**Giải pháp:** Chạy file `Server/add_account_status.sql`

### Lỗi: Không thể tạo tài khoản
**Nguyên nhân:** File `createUser.php` không có quyền write
**Giải pháp:** Kiểm tra quyền folder Server

### Lỗi: Switch không hoạt động
**Nguyên nhân:** API `updateUserStatus.php` chưa được deploy
**Giải pháp:** Copy file vào folder Server

### Tài khoản bị khóa vẫn đăng nhập được
**Nguyên nhân:** File `dangnhap.php` chưa được cập nhật
**Giải pháp:** Replace file `dangnhap.php` mới

---

## 📊 DATABASE SCHEMA

```sql
CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) NOT NULL,
    pass VARCHAR(255) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    role INT DEFAULT 0 COMMENT '0=User, 1=Admin',
    status INT DEFAULT 1 COMMENT '1=Active, 0=Locked',
    loginType VARCHAR(50) DEFAULT 'normal',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status (status)
);
```

---

## ✨ TÍNH NĂNG BỔ SUNG

### Có thể mở rộng:
- [ ] Lịch sử thay đổi trạng thái tài khoản
- [ ] Tự động khóa tài khoản sau N lần đăng nhập sai
- [ ] Thông báo email khi tài khoản bị khóa
- [ ] Lý do khóa tài khoản (textarea)
- [ ] Khóa tạm thời (tự động mở sau X ngày)

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra log trong Logcat (Android Studio)
2. Kiểm tra error_log của Apache/XAMPP
3. Kiểm tra database có cột `status` chưa
4. Đảm bảo tất cả file PHP đã được copy vào Server

---

**Ngày tạo:** 2025-01-11
**Phiên bản:** 1.0
**Tương thích:** Android 8.0+ / PHP 7.0+ / MySQL 5.7+

