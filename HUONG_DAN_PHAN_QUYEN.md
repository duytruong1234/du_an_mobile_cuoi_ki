# HƯỚNG DẪN CÀI ĐẶT HỆ THỐNG PHÂN QUYỀN

## 🔐 Tính năng đã thêm

Dự án đã được cập nhật với **hệ thống phân quyền** đầy đủ:
- ✅ **CHỈ TÀI KHOẢN ADMIN** có quyền thêm, sửa, xóa sản phẩm
- ✅ User thường chỉ có thể xem và mua sản phẩm
- ✅ Menu "Quản lí" chỉ hiển thị cho admin
- ✅ Kiểm tra quyền truy cập khi vào các màn hình quản lý

## 📋 Các file đã thay đổi

### 1. Model (Java)
- **User.java**: Thêm trường `role` và method `isAdmin()`
  - `role = 0`: User thường
  - `role = 1`: Admin

### 2. Activity (Java)
- **MainActivity.java**: Menu "Quản lí" chỉ hiển thị cho admin
- **QuanLiActivity.java**: Kiểm tra quyền admin khi vào màn hình quản lý sản phẩm
- **ThemSPActivity.java**: Kiểm tra quyền admin khi thêm/sửa sản phẩm

### 3. Backend (PHP)
- **connect.php**: File kết nối database
- **dangnhap.php**: API đăng nhập (có trả về thông tin role)
- **dangki.php**: API đăng ký (user mới mặc định là user thường)

### 4. Database
- **update_database_add_role.sql**: Script cập nhật database

## 🚀 Cách cài đặt

### Bước 1: Cập nhật Database
1. Mở **phpMyAdmin** (hoặc MySQL Workbench)
2. Chọn database `appbandienthoai`
3. Vào tab **SQL**
4. Copy toàn bộ nội dung file `update_database_add_role.sql`
5. Paste và click **Go** để chạy script

Script sẽ:
- Thêm cột `role` vào bảng `user`
- Tạo tài khoản admin mặc định:
  - **Email**: `admin@admin.com`
  - **Password**: `admin123`
  - **Role**: 1 (Admin)

### Bước 2: Kiểm tra Backend PHP
Đảm bảo các file PHP đã được tạo ở thư mục gốc:
```
D:\Appmanager\AppBanDienThoai\
├── connect.php          ✅ Mới tạo
├── dangnhap.php        ✅ Đã cập nhật
├── dangki.php          ✅ Đã cập nhật
└── update_database_add_role.sql  ✅ Mới tạo
```

### Bước 3: Build lại ứng dụng Android
1. Mở project trong Android Studio
2. Click **Build** → **Rebuild Project**
3. Chạy ứng dụng trên emulator hoặc thiết bị thật

## 🧪 Cách kiểm tra

### Test với tài khoản Admin:
1. Đăng nhập với:
   - Email: `admin@admin.com`
   - Password: `admin123`
2. Kiểm tra menu drawer:
   - ✅ Phải thấy mục **"Quản lí"**
3. Click vào "Quản lí":
   - ✅ Vào được màn hình quản lý sản phẩm
   - ✅ Có thể thêm/sửa/xóa sản phẩm

### Test với tài khoản User thường:
1. Đăng ký tài khoản mới hoặc dùng tài khoản user hiện có
2. Kiểm tra menu drawer:
   - ✅ **KHÔNG** thấy mục "Quản lí"
3. Nếu cố tình mở QuanLiActivity hoặc ThemSPActivity:
   - ✅ Sẽ thấy thông báo: "Bạn không có quyền truy cập chức năng này! Chỉ admin mới được phép."
   - ✅ Activity tự động đóng

## 🔑 Quản lý tài khoản Admin

### Cách tạo thêm admin:
**KHÔNG THỂ tạo admin qua ứng dụng!** 

Để bảo mật, bạn phải cập nhật trực tiếp trong database:

1. Vào phpMyAdmin
2. Chọn database `appbandienthoai`
3. Vào bảng `user`
4. Tìm user cần nâng quyền
5. Sửa cột `role` thành `1`
6. Click **Go** để lưu

Hoặc chạy SQL:
```sql
UPDATE user SET role = 1 WHERE email = 'email_user_can_nang_quyen@example.com';
```

### Cách hạ quyền admin về user thường:
```sql
UPDATE user SET role = 0 WHERE email = 'email_admin_can_ha_quyen@example.com';
```

## ⚠️ Lưu ý quan trọng

1. **Mật khẩu mặc định**: 
   - Sau khi cài đặt, nên đổi mật khẩu admin ngay
   - Hiện tại `admin123` không bảo mật

2. **Bảo mật**:
   - Backend PHP hiện tại CHƯA mã hóa mật khẩu
   - Nên thêm mã hóa MD5 hoặc bcrypt cho password

3. **SQL Injection**:
   - Backend hiện tại CHƯA dùng Prepared Statements
   - Có thể bị SQL injection
   - Nên cập nhật sau để bảo mật hơn

4. **Chỉ admin mới có quyền**:
   - Thêm sản phẩm
   - Sửa sản phẩm  
   - Xóa sản phẩm
   - Truy cập màn hình "Quản lí"

## 📊 Cấu trúc bảng User sau khi cập nhật

```sql
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `pass` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `mobile` varchar(20) NOT NULL,
  `role` int(11) NOT NULL DEFAULT 0 COMMENT '0=user thường, 1=admin',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
);
```

## ✅ Kết luận

Hệ thống phân quyền đã được triển khai hoàn chỉnh:
- ✅ Backend PHP hỗ trợ role
- ✅ Model Java có trường role
- ✅ Kiểm tra quyền ở tất cả màn hình quản lý
- ✅ Menu hiển thị theo quyền
- ✅ Tài khoản admin mặc định đã tạo

**Chỉ có 1 tài khoản admin (hoặc nhiều nếu bạn tự tạo trong database) mới có quyền thêm, sửa, xóa sản phẩm!**

