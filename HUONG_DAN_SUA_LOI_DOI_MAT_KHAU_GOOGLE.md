# Hướng Dẫn Sửa Lỗi Đổi Mật Khẩu Tài Khoản Google

## Vấn Đề
Khi đăng nhập bằng Google, sau đó đổi mật khẩu, rồi đăng xuất và đăng nhập lại bằng Google thì báo sai mật khẩu.

## Nguyên Nhân
- Khi đăng nhập Google, hệ thống tạo mật khẩu mặc định dựa trên email
- Khi đổi mật khẩu, mật khẩu mới được lưu vào database
- Khi đăng nhập lại bằng Google, hệ thống dùng mật khẩu mặc định cũ → Sai mật khẩu!

## Giải Pháp
Đã thêm cột `login_type` để phân biệt tài khoản thường và Google, và **không cho phép đổi mật khẩu** cho tài khoản Google.

---

## Các Bước Thực Hiện

### Bước 1: Chạy SQL để thêm cột `login_type`

1. Mở **phpMyAdmin** (http://localhost/phpmyadmin)
2. Chọn database `appbandongho`
3. Nhấn tab **SQL**
4. Copy và chạy đoạn SQL sau:

```sql
-- Thêm cột login_type vào bảng user
ALTER TABLE `user` ADD COLUMN `login_type` VARCHAR(20) DEFAULT 'normal' AFTER `role`;

-- Cập nhật các tài khoản Google hiện tại (có mobile = '0000000000')
UPDATE `user` SET `login_type` = 'google' WHERE `mobile` = '0000000000';

-- Kiểm tra kết quả
SELECT id, email, username, mobile, login_type FROM `user`;
```

5. Nhấn **Go** để thực thi

**Hoặc import file SQL:**
- Mở phpMyAdmin → Import
- Chọn file: `add_login_type_fixed.sql`
- Nhấn **Go**

### Bước 2: Kiểm tra cấu trúc bảng

Sau khi chạy SQL, kiểm tra bảng `user` phải có cột `login_type`:

| id | email | username | mobile | role | login_type |
|----|-------|----------|--------|------|------------|
| 1  | test@gmail.com | Test User | 0000000000 | 0 | **google** |
| 2  | normal@gmail.com | Normal | 0123456789 | 0 | **normal** |

### Bước 3: Rebuild App

1. Trong Android Studio, chọn **Build → Clean Project**
2. Sau đó **Build → Rebuild Project**
3. Chờ build hoàn tất

### Bước 4: Test Lại

#### Test 1: Đăng nhập Google
1. Mở app
2. Đăng nhập bằng Google
3. Vào **Cập nhật thông tin cá nhân**
4. **Không thấy ô nhập mật khẩu** ✅
5. Thấy thông báo: "Tài khoản Google không thể đổi mật khẩu tại đây"

#### Test 2: Đăng nhập thường
1. Đăng xuất
2. Đăng ký tài khoản mới bằng email/password thường
3. Đăng nhập
4. Vào **Cập nhật thông tin cá nhân**
5. **Vẫn thấy ô nhập mật khẩu** ✅
6. Có thể đổi mật khẩu bình thường

#### Test 3: Đăng nhập lại sau khi đổi mật khẩu
1. Với tài khoản Google: Đăng xuất → Đăng nhập lại bằng Google → **Thành công** ✅
2. Với tài khoản thường: Đổi mật khẩu → Đăng xuất → Đăng nhập với mật khẩu mới → **Thành công** ✅

---

## Các Thay Đổi Đã Thực Hiện

### 1. Database (SQL)
- ✅ Thêm cột `login_type` vào bảng `user`
- ✅ Cập nhật tài khoản Google hiện tại

### 2. Backend (PHP)
- ✅ `Server/dangki.php`: Tự động set `login_type = 'google'` nếu `mobile = '0000000000'`
- ✅ `Server/updateProfile.php`: Không cho phép đổi mật khẩu nếu `login_type = 'google'`

### 3. Frontend (Android)
- ✅ `User.java`: Thêm field `loginType` và method `isGoogleAccount()`
- ✅ `UpdateProfileActivity.java`: Ẩn ô mật khẩu cho tài khoản Google

---

## Lưu Ý Quan Trọng

### Tài Khoản Google
- ❌ **KHÔNG** thể đổi mật khẩu trong app
- ✅ Mật khẩu được quản lý bởi Google
- ✅ Luôn đăng nhập được bằng Google Sign-In

### Tài Khoản Thường
- ✅ Có thể đổi mật khẩu tự do
- ✅ Đăng nhập bằng email + mật khẩu

---

## Troubleshooting

### Vấn đề: Vẫn báo sai mật khẩu khi đăng nhập Google

**Giải pháp 1:** Reset mật khẩu cho tài khoản Google về mật khẩu mặc định
```sql
-- Tìm email của bạn
SELECT * FROM user WHERE email = 'your-email@gmail.com';

-- Nếu là tài khoản Google, set lại mật khẩu mặc định
UPDATE user SET 
    login_type = 'google',
    mobile = '0000000000'
WHERE email = 'your-email@gmail.com';
```

**Giải pháp 2:** Xóa tài khoản và đăng ký lại
```sql
DELETE FROM user WHERE email = 'your-email@gmail.com';
```
Sau đó đăng nhập lại bằng Google trong app.

### Vấn đề: Không thấy cột login_type

Chạy lại SQL:
```sql
ALTER TABLE `user` ADD COLUMN `login_type` VARCHAR(20) DEFAULT 'normal' AFTER `role`;
```

### Vấn đề: App báo lỗi khi build

1. Clean Project: **Build → Clean Project**
2. Invalidate Caches: **File → Invalidate Caches / Restart...**
3. Rebuild: **Build → Rebuild Project**

---

## Kết Luận

✅ **Đã sửa xong lỗi!**

- Tài khoản Google giờ không thể đổi mật khẩu → Không bị conflict
- Đăng nhập Google luôn hoạt động ổn định
- Tài khoản thường vẫn đổi mật khẩu được bình thường

🎉 Bây giờ bạn có thể đăng nhập Google mà không lo bị lỗi sai mật khẩu nữa!

