# HƯỚNG DẪN QUẢN LÝ NGƯỜI DÙNG CHO ADMIN

## Tính năng mới đã được thêm vào

### 1. Màn hình Quản Lý Người Dùng (QuanLyNguoiDungActivity)

Admin có thể:
- **Xem danh sách tất cả người dùng** trong hệ thống
- **Phân quyền Admin/User** bằng cách bật/tắt switch
- **Xóa người dùng** (không thể xóa chính mình)
- Xem thông tin chi tiết: tên, email, số điện thoại, loại đăng nhập (Thường/Google)

### 2. Các file đã tạo

#### Android App:
1. **QuanLyNguoiDungActivity.java** - Activity quản lý người dùng
2. **NguoiDungAdapter.java** - Adapter hiển thị danh sách người dùng
3. **activity_quan_ly_nguoi_dung.xml** - Layout cho màn hình quản lý
4. **item_nguoi_dung.xml** - Layout cho mỗi item người dùng

#### Server PHP:
1. **getAllUsers.php** - API lấy danh sách tất cả người dùng
2. **updateUserRole.php** - API cập nhật quyền admin/user
3. **deleteUser.php** - API xóa người dùng

#### Cập nhật:
- **ApiBanHang.java** - Thêm 3 API endpoints mới
- **activity_quan_li.xml** - Thêm nút "Quản lý người dùng"
- **QuanLiActivity.java** - Thêm xử lý mở màn hình quản lý người dùng
- **AndroidManifest.xml** - Đăng ký QuanLyNguoiDungActivity

## Cách sử dụng

### Bước 1: Upload file PHP lên server
Copy 3 file PHP vào thư mục Server trên hosting:
- `getAllUsers.php`
- `updateUserRole.php`
- `deleteUser.php`

### Bước 2: Chạy lại ứng dụng Android
```bash
# Build lại project
gradlew clean
gradlew build
```

### Bước 3: Truy cập chức năng quản lý người dùng
1. Đăng nhập bằng **tài khoản Admin** (role = 1)
2. Vào menu **Quản Lý** từ MainActivity
3. Nhấn vào biểu tượng **người dùng** ở toolbar (bên trái nút thống kê)
4. Màn hình quản lý người dùng sẽ hiển thị

### Bước 4: Quản lý người dùng
- **Phân quyền Admin**: Bật/tắt switch "Quyền Admin"
- **Xóa người dùng**: Nhấn vào icon thùng rác màu đỏ
- **Xem thông tin**: Tất cả thông tin hiển thị trên card

## Giao diện

### Màn hình Quản Lý Người Dùng
```
┌─────────────────────────────────┐
│ ← Quản Lý Người Dùng            │
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ Nguyễn Văn A        [ADMIN] │ │
│ │ 📧 email@example.com        │ │
│ │ 📞 0123456789               │ │
│ │ Loại: Thường                │ │
│ │ ─────────────────────────── │ │
│ │ Quyền Admin: [ON]  🗑️       │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Trần Thị B                  │ │
│ │ 📧 user2@gmail.com          │ │
│ │ 📞 0987654321               │ │
│ │ Loại: Google                │ │
│ │ ─────────────────────────── │ │
│ │ Quyền Admin: [OFF] 🗑️       │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

## API Endpoints

### 1. GET getAllUsers.php
**Mô tả**: Lấy danh sách tất cả người dùng

**Response**:
```json
{
  "success": true,
  "message": "Lấy danh sách người dùng thành công",
  "result": [
    {
      "id": 1,
      "email": "admin@example.com",
      "username": "Admin",
      "mobile": "0123456789",
      "role": 1,
      "login_type": "normal"
    },
    {
      "id": 2,
      "email": "user@gmail.com",
      "username": "User",
      "mobile": "0000000000",
      "role": 0,
      "login_type": "google"
    }
  ]
}
```

### 2. POST updateUserRole.php
**Parameters**:
- `userid` (int): ID người dùng
- `role` (int): Quyền mới (0 = user, 1 = admin)

**Response**:
```json
{
  "success": true,
  "message": "Đã cập nhật quyền thành Admin",
  "result": {
    "userid": 2,
    "role": 1
  }
}
```

### 3. POST deleteUser.php
**Parameters**:
- `userid` (int): ID người dùng cần xóa

**Response**:
```json
{
  "success": true,
  "message": "Đã xóa người dùng Nguyễn Văn A",
  "result": {
    "userid": 2
  }
}
```

## Bảo mật

### Kiểm tra quyền Admin
```java
// Kiểm tra khi mở activity
if (Utils.user_current == null || !Utils.user_current.isAdmin()) {
    Toast.makeText(this, "Bạn không có quyền truy cập!", Toast.LENGTH_LONG).show();
    finish();
    return;
}
```

### Không cho phép xóa chính mình
```java
if (user.getId() == Utils.user_current.getId()) {
    Toast.makeText(this, "Không thể xóa tài khoản của chính mình!", Toast.LENGTH_SHORT).show();
    return;
}
```

## Lưu ý quan trọng

1. **Chỉ Admin mới truy cập được**: Activity tự động kiểm tra và đóng nếu không phải admin
2. **Không xóa được chính mình**: Tránh tình huống admin tự xóa tài khoản của mình
3. **Xác nhận trước khi xóa**: Dialog xác nhận hiển thị trước khi xóa người dùng
4. **Cập nhật realtime**: Danh sách tự động cập nhật sau mỗi thao tác

## Cải tiến trong tương lai

1. **Phân trang**: Hiển thị nhiều người dùng với pagination
2. **Tìm kiếm**: Tìm kiếm người dùng theo tên, email
3. **Lọc**: Lọc theo role, loại đăng nhập
4. **Soft delete**: Xóa mềm thay vì xóa hẳn khỏi database
5. **Kiểm tra admin từ server**: Validate quyền admin ở server-side
6. **Xem lịch sử**: Xem lịch sử đơn hàng của từng người dùng
7. **Khóa tài khoản**: Tính năng tạm khóa thay vì xóa

## Troubleshooting

### Lỗi: "Không kết nối được server"
- Kiểm tra URL trong `Utils.BASE_URL`
- Đảm bảo file PHP đã upload lên server
- Kiểm tra kết nối internet

### Lỗi: "Không có quyền truy cập"
- Đảm bảo đăng nhập bằng tài khoản admin (role = 1)
- Kiểm tra database, đảm bảo user có role = 1

### Danh sách trống
- Kiểm tra database có dữ liệu user không
- Xem log để debug: `Log.d("QuanLyNguoiDung", ...)`

## Kết luận

Chức năng quản lý người dùng đã được tích hợp hoàn chỉnh vào ứng dụng. Admin có thể dễ dàng quản lý, phân quyền và xóa người dùng từ ứng dụng Android mà không cần truy cập database trực tiếp.

