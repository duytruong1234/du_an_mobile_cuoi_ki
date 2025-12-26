# Hướng Dẫn Sử Dụng Chức Năng Quản Lý Người Dùng

## 📋 Tổng Quan

Chức năng quản lý người dùng cho phép Admin:
- Xem danh sách tất cả người dùng
- Phân quyền Admin/User
- Xóa người dùng
- Phân biệt tài khoản Google và tài khoản thường

## 🚀 Cách Truy Cập

### Từ Màn Hình Quản Lý Sản Phẩm:
1. Đăng nhập với tài khoản Admin
2. Mở menu bên trái → chọn "Quản lí"
3. Trên toolbar, nhấn vào icon **Quản lý người dùng** (icon người dùng bên trái)
4. Màn hình "Quản Lý Người Dùng" sẽ hiển thị

## ✨ Tính Năng

### 1. **Xem Danh Sách Người Dùng**
- Hiển thị tất cả người dùng trong hệ thống
- Thông tin hiển thị:
  - Tên người dùng
  - Email
  - Số điện thoại
  - Loại tài khoản (Google/Thường)
  - Badge ADMIN (nếu là admin)

### 2. **Phân Quyền Admin**
- Sử dụng **Switch** bên phải mỗi người dùng
- Bật = Admin (role = 1)
- Tắt = User thường (role = 0)
- Thay đổi được áp dụng ngay lập tức

### 3. **Xóa Người Dùng**
- Nhấn vào icon **Thùng rác** màu đỏ
- Xác nhận xóa trong dialog
- **Lưu ý**: Không thể xóa chính tài khoản mình

### 4. **Phân Biệt Loại Tài Khoản**
- **Google**: Tài khoản đăng nhập qua Google (mobile = "0000000000")
- **Thường**: Tài khoản đăng ký thông thường

## 🔐 Bảo Mật

### Kiểm Tra Quyền Admin:
Chỉ Admin mới có thể:
- Truy cập màn hình quản lý người dùng
- Xem danh sách users
- Thay đổi quyền
- Xóa người dùng

Nếu user thường cố gắng truy cập → Hiển thị thông báo lỗi và tự động đóng màn hình.

## 📡 API Endpoints

### 1. **Lấy Danh Sách Người Dùng**
```
GET: getAllUsers.php
Response: {
    "success": true,
    "message": "Lấy danh sách người dùng thành công",
    "result": [
        {
            "id": 1,
            "email": "admin@admin.com",
            "username": "Admin",
            "mobile": "0123456789",
            "role": 1,
            "login_type": "normal"
        }
    ]
}
```

### 2. **Cập Nhật Quyền**
```
POST: updateUserRole.php
Params:
- userid: int (ID người dùng)
- role: int (0 = user, 1 = admin)

Response: {
    "success": true,
    "message": "Đã cập nhật quyền thành Admin"
}
```

### 3. **Xóa Người Dùng**
```
POST: deleteUser.php
Params:
- userid: int (ID người dùng)

Response: {
    "success": true,
    "message": "Đã xóa người dùng [tên]"
}
```

## 🎨 Giao Diện

### Layout Files:
- `activity_quan_ly_nguoi_dung.xml` - Màn hình chính
- `item_nguoi_dung.xml` - Item trong RecyclerView

### Components:
- **Toolbar** với nút back
- **RecyclerView** hiển thị danh sách users
- **ProgressBar** khi đang tải
- **CardView** cho mỗi user item

### Item User Bao Gồm:
- TextView: Username (bold)
- TextView: Email với icon
- TextView: Mobile với icon
- TextView: Login Type (italic, nhỏ)
- TextView: Admin Badge (màu đỏ, chỉ hiện khi là admin)
- Switch: Toggle Admin role
- ImageView: Delete button (icon thùng rác màu đỏ)

## 🔧 Files Liên Quan

### Android (Java):
```
app/src/main/java/vn/duytruong/appbandienthoai/
├── activity/
│   └── QuanLyNguoiDungActivity.java
├── adapter/
│   └── NguoiDungAdapter.java
├── model/
│   └── User.java
└── retrofit/
    └── ApiBanHang.java (interface)
```

### Server (PHP):
```
D:/AppBanDongHo/
├── getAllUsers.php
├── updateUserRole.php
└── deleteUser.php
```

## ⚠️ Lưu Ý

1. **Xóa Người Dùng**:
   - Không thể xóa chính mình
   - Khi xóa user, các đơn hàng liên quan có thể bị ảnh hưởng
   - Nên cân nhắc sử dụng "soft delete" trong production

2. **Phân Quyền**:
   - Thay đổi quyền có hiệu lực ngay lập tức
   - User bị hạ quyền sẽ mất quyền truy cập các chức năng admin

3. **Tài Khoản Google**:
   - Được đánh dấu với login_type = "google"
   - Có mobile = "0000000000"
   - Có thể phân quyền admin như tài khoản thường

## 🧪 Test Checklist

- [ ] Đăng nhập với tài khoản admin
- [ ] Mở màn hình quản lý người dùng
- [ ] Kiểm tra danh sách hiển thị đầy đủ
- [ ] Thử toggle role user → admin
- [ ] Thử toggle role admin → user
- [ ] Thử xóa một user (không phải mình)
- [ ] Thử xóa chính mình (phải báo lỗi)
- [ ] Đăng xuất và đăng nhập với tài khoản user thường
- [ ] Kiểm tra không thể truy cập quản lý người dùng

## 🎯 Kết Luận

Chức năng quản lý người dùng đã được tích hợp hoàn chỉnh với:
- ✅ Giao diện trực quan, dễ sử dụng
- ✅ Phân quyền chặt chẽ (chỉ admin)
- ✅ API bảo mật với error handling
- ✅ Real-time update không cần refresh
- ✅ Phân biệt tài khoản Google/Thường

**Tác giả**: GitHub Copilot  
**Ngày tạo**: 29/10/2025  
**Phiên bản**: 1.0

