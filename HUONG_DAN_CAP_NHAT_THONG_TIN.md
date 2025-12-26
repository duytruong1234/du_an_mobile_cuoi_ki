# HƯỚNG DẪN SỬ DỤNG CHỨC NĂNG CẬP NHẬT THÔNG TIN CÁ NHÂN

## 📋 Tổng quan
Chức năng cho phép người dùng cập nhật thông tin cá nhân bao gồm:
- ✅ Tên hiển thị (username)
- ✅ Email
- ✅ Số điện thoại
- ✅ Đổi mật khẩu (tùy chọn)

---

## 🚀 Cách sử dụng

### 1. Truy cập màn hình cập nhật thông tin
- Mở app và đăng nhập
- Mở menu bên trái (navigation drawer)
- Click vào "Thông tin cá nhân" hoặc "Profile"

### 2. Cập nhật thông tin
- Các trường sẽ tự động điền thông tin hiện tại
- Sửa đổi thông tin muốn thay đổi:
  - **Tên hiển thị**: Bắt buộc, không được để trống
  - **Email**: Bắt buộc, phải đúng định dạng email
  - **Số điện thoại**: Tùy chọn, nếu nhập phải có ít nhất 10 số
  
### 3. Đổi mật khẩu (tùy chọn)
- Nếu muốn đổi mật khẩu:
  - Nhập mật khẩu mới (tối thiểu 6 ký tự)
  - Nhập lại để xác nhận
- Nếu KHÔNG muốn đổi mật khẩu:
  - Để trống 2 trường mật khẩu

### 4. Lưu thay đổi
- Click nút **"Cập nhật thông tin"**
- Đợi xử lý (hiện progress bar)
- Thông báo kết quả
- Tự động đóng màn hình sau 1 giây nếu thành công

---

## 🎨 Giao diện (UX cơ bản)

### Thiết kế:
✅ **Avatar placeholder** - Icon người dùng tròn ở đầu trang
✅ **ID hiển thị** - Hiện ID user phía dưới avatar
✅ **Material Design TextInputLayout** - Input fields đẹp với icon
✅ **Icon trực quan**:
   - 👤 User icon cho tên
   - 📧 Email icon cho email
   - 📱 Phone icon cho số điện thoại
   - 🔒 Lock icon cho mật khẩu
✅ **Toggle password visibility** - Nút hiện/ẩn mật khẩu
✅ **Divider** - Phân tách phần thông tin cơ bản và đổi mật khẩu
✅ **Progress bar** - Hiện khi đang xử lý
✅ **Validation realtime** - Hiện lỗi ngay khi nhập sai

---

## 🔒 Bảo mật

### Validation:
- ✅ Kiểm tra định dạng email
- ✅ Kiểm tra độ dài số điện thoại
- ✅ Kiểm tra độ dài mật khẩu (min 6 ký tự)
- ✅ Kiểm tra mật khẩu xác nhận khớp
- ✅ Kiểm tra email trùng với user khác

### Server-side:
- ✅ Validate tất cả input
- ✅ Escape SQL injection
- ✅ Hash mật khẩu với MD5 (giống đăng ký)
- ✅ Kiểm tra quyền sở hữu tài khoản
- ✅ Kiểm tra email trùng lặp

---

## 📁 Files đã tạo

### Backend (PHP):
```
Server/updateProfile.php
```

### Frontend (Android):
```
app/src/main/java/vn/duytruong/appbandienthoai/activity/UpdateProfileActivity.java
app/src/main/res/layout/activity_update_profile.xml
app/src/main/res/drawable/bg_circle_gray.xml
app/src/main/res/drawable/bg_button_primary.xml
app/src/main/res/drawable/ic_user.xml
app/src/main/res/drawable/ic_email.xml
app/src/main/res/drawable/ic_phone.xml
app/src/main/res/drawable/ic_lock.xml
app/src/main/res/drawable/ic_user_placeholder.xml
```

### Cấu hình:
- ✅ Đã đăng ký Activity trong `AndroidManifest.xml`
- ✅ Đã tích hợp vào menu `MainActivity.java`

---

## 🔧 API Endpoint

### URL:
```
POST http://your-server/appbandienthoai/updateProfile.php
```

### Parameters:
```
iduser: int (required)
username: string (required)
email: string (required)
mobile: string (optional)
password: string (optional - chỉ gửi khi muốn đổi)
```

### Response Success:
```json
{
  "success": true,
  "message": "Cập nhật thông tin thành công",
  "user": {
    "id": 123,
    "username": "Nguyen Van A",
    "email": "nguyenvana@gmail.com",
    "mobile": "0123456789",
    "role": 0
  }
}
```

### Response Error:
```json
{
  "success": false,
  "message": "Email này đã được sử dụng bởi tài khoản khác"
}
```

---

## ✅ Test Cases

### Test 1: Cập nhật thành công tất cả thông tin
1. Đăng nhập
2. Mở "Thông tin cá nhân"
3. Đổi tên, email, số điện thoại
4. Nhập mật khẩu mới + xác nhận
5. Click "Cập nhật thông tin"
6. ✅ Thành công, thông tin được lưu

### Test 2: Cập nhật không đổi mật khẩu
1. Chỉ sửa tên và email
2. Để trống 2 trường mật khẩu
3. Click "Cập nhật thông tin"
4. ✅ Thành công, mật khẩu cũ giữ nguyên

### Test 3: Validation lỗi
- ❌ Để trống tên → Hiện lỗi "Tên hiển thị không được để trống"
- ❌ Email sai format → "Email không đúng định dạng"
- ❌ Mật khẩu < 6 ký tự → "Mật khẩu phải có ít nhất 6 ký tự"
- ❌ Mật khẩu xác nhận không khớp → "Mật khẩu xác nhận không khớp"
- ❌ Email đã tồn tại → "Email này đã được sử dụng bởi tài khoản khác"

---

## 🐛 Troubleshooting

### Lỗi: Không mở được màn hình
- ✅ Kiểm tra đã đăng ký Activity trong AndroidManifest.xml
- ✅ Rebuild project

### Lỗi: Không kết nối được API
- ✅ Kiểm tra file `updateProfile.php` đã upload lên server
- ✅ Kiểm tra URL trong `Utils.BASE_URL`
- ✅ Kiểm tra internet và server đang chạy

### Lỗi: Email đã tồn tại
- ✅ Đổi email khác
- ✅ Hoặc giữ nguyên email hiện tại

---

## 🎯 Tính năng tương lai (có thể mở rộng)

- 📸 Upload avatar
- 📍 Thêm địa chỉ mặc định
- 🎂 Ngày sinh
- 👤 Giới tính
- 🔔 Cài đặt thông báo
- 🔐 Xác thực 2 lớp (2FA)
- 📊 Lịch sử thay đổi thông tin

---

## 📞 Hỗ trợ

Nếu gặp vấn đề, kiểm tra:
1. Log Android: Tìm tag "UpdateProfileActivity"
2. Server log: Kiểm tra file PHP có lỗi gì không
3. Network: Dùng Postman test API trực tiếp

**Hoàn thành!** ✅

