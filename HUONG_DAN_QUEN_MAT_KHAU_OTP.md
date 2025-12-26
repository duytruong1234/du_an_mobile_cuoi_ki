# HƯỚNG DẪN CHỨC NĂNG QUÊN MẬT KHẨU - APP BÁN ĐỒNG HỒ

## 📋 TỔNG QUAN

Chức năng quên mật khẩu cho phép người dùng đặt lại mật khẩu thông qua email với mã OTP bảo mật.

## 🔄 LUỒNG HOẠT ĐỘNG

```
1. User nhập email → Gửi request
   ↓
2. Server tạo OTP → Lưu DB → GỬI EMAIL
   ↓
3. App thông báo: "Mã OTP đã được gửi đến email"
   ↓
4. User mở email → Copy mã OTP
   ↓
5. Quay lại app → Nhập OTP → Đổi mật khẩu
   ↓
6. Thành công → Quay về màn hình đăng nhập
```

## 🛠️ CÀI ĐẶT VÀ CẤU HÌNH

### Bước 1: Cập nhật Database

Chạy file SQL để thêm các cột cần thiết:

```sql
-- File: update_reset_password_otp.sql
ALTER TABLE `user` ADD COLUMN `reset_otp` VARCHAR(6) DEFAULT NULL AFTER `login_type`;
ALTER TABLE `user` ADD COLUMN `reset_otp_expiry` DATETIME DEFAULT NULL AFTER `reset_otp`;
```

**Cách chạy:**
1. Mở phpMyAdmin
2. Chọn database `appbandongho`
3. Vào tab SQL
4. Copy nội dung file `update_reset_password_otp.sql` và Execute

### Bước 2: Cài đặt PHPMailer

Để gửi email OTP, bạn cần cài đặt PHPMailer:

#### Cách 1: Dùng Composer (Khuyến nghị)
```bash
cd Server
composer require phpmailer/phpmailer
```

#### Cách 2: Download thủ công
1. Download PHPMailer từ: https://github.com/PHPMailer/PHPMailer/releases
2. Giải nén vào thư mục `Server/PHPMailer/`
3. Cấu trúc thư mục:
   ```
   Server/
   ├── PHPMailer/
   │   └── src/
   │       ├── PHPMailer.php
   │       ├── SMTP.php
   │       └── Exception.php
   ```

### Bước 3: Cấu hình Email

Mở file `Server/email_config.php` và cập nhật thông tin:

```php
$mail->Username   = 'your-email@gmail.com'; // ⚠️ Email của bạn
$mail->Password   = 'your-app-password';    // ⚠️ App Password (KHÔNG phải mật khẩu Gmail)
```

#### Cách lấy App Password từ Gmail:

1. Đăng nhập Gmail → Cài đặt
2. Tìm "Xác minh 2 bước" → Bật nó
3. Quay lại → Tìm "Mật khẩu ứng dụng"
4. Chọn "Ứng dụng khác" → Nhập tên "App Ban Dong Ho"
5. Click "Tạo" → Copy mã 16 ký tự
6. Dán vào `$mail->Password`

### Bước 4: Upload Files lên Server

Upload các file sau lên server:

```
Server/
├── email_config.php              ← CẤU HÌNH EMAIL
├── reset_pass.php                ← API GỬI OTP
├── verify_otp_reset_pass.php     ← API XÁC THỰC OTP VÀ ĐỔI MẬT KHẨU
└── PHPMailer/                    ← THƯ VIỆN GỬI EMAIL
```

## 📱 SỬ DỤNG TRONG APP

### 1. Từ màn hình Đăng nhập

- Click vào text "Quên mật khẩu?"
- Sẽ mở màn hình Reset Password

### 2. Nhập email

- Nhập email đã đăng ký
- Click "Gửi mã OTP"
- Đợi thông báo "Mã OTP đã được gửi đến email của bạn"

### 3. Kiểm tra email

- Mở email (có thể ở hộp thư đến hoặc spam)
- Tìm email từ "App Bán Đồng Hồ"
- Copy mã OTP 6 số

### 4. Nhập OTP và mật khẩu mới

- Quay lại app
- Nhập mã OTP vừa nhận
- Nhập mật khẩu mới (tối thiểu 6 ký tự)
- Xác nhận lại mật khẩu
- Click "Đổi mật khẩu"

### 5. Đăng nhập lại

- Sau khi đổi thành công, quay về màn hình đăng nhập
- Đăng nhập bằng email và mật khẩu mới

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. Tài khoản Google
- Tài khoản đăng nhập bằng Google **KHÔNG THỂ** đổi mật khẩu
- Hệ thống sẽ thông báo: "Tài khoản Google không thể đổi mật khẩu. Vui lòng đăng nhập bằng Google."

### 2. Thời gian hiệu lực OTP
- Mã OTP có hiệu lực **5 phút**
- Sau 5 phút phải gửi lại OTP mới

### 3. Email không nhận được OTP
- Kiểm tra hộp thư **Spam/Junk**
- Kiểm tra email nhập đúng chưa
- Kiểm tra kết nối internet
- Thử gửi lại OTP

### 4. Bảo mật
- Không chia sẻ mã OTP với ai
- Mỗi mã OTP chỉ dùng được 1 lần
- Mật khẩu mới phải khác mật khẩu cũ (khuyến nghị)

## 🔧 XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi: "Không thể gửi email"

**Nguyên nhân:**
- Sai cấu hình email
- Chưa bật "Xác minh 2 bước" trong Gmail
- App Password không đúng

**Giải pháp:**
1. Kiểm tra lại `email_config.php`
2. Đảm bảo đã bật "Xác minh 2 bước"
3. Tạo lại App Password mới

### Lỗi: "Mã OTP không chính xác"

**Nguyên nhân:**
- Nhập sai mã OTP
- Đã hết thời gian 5 phút

**Giải pháp:**
1. Kiểm tra lại mã OTP trong email
2. Gửi lại OTP mới

### Lỗi: "Mã OTP đã hết hạn"

**Giải pháp:**
- Click "Gửi lại OTP" để nhận mã mới

### Lỗi: "Email không tồn tại trong hệ thống"

**Giải pháp:**
- Kiểm tra lại email đã đăng ký chưa
- Nếu chưa có tài khoản, click "Đăng ký"

## 📊 CẤU TRÚC DATABASE

```sql
Table: user
├── id (int, primary key)
├── email (varchar)
├── pass (varchar, md5 hashed)
├── username (varchar)
├── mobile (varchar)
├── login_type (varchar) - 'normal' hoặc 'google'
├── reset_otp (varchar(6)) - Mã OTP 6 số
└── reset_otp_expiry (datetime) - Thời gian hết hạn OTP
```

## 📡 API ENDPOINTS

### 1. Gửi OTP
```
POST: reset_pass.php
Parameters:
  - email: string (required)

Response Success:
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.",
  "result": {
    "email": "user@example.com",
    "otp_expiry": "5 phút"
  }
}

Response Error:
{
  "success": false,
  "message": "Email không tồn tại trong hệ thống"
}
```

### 2. Xác thực OTP và đổi mật khẩu
```
POST: verify_otp_reset_pass.php
Parameters:
  - email: string (required)
  - otp: string (required, 6 digits)
  - new_password: string (required, min 6 chars)

Response Success:
{
  "success": true,
  "message": "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.",
  "result": {
    "email": "user@example.com"
  }
}

Response Error:
{
  "success": false,
  "message": "Mã OTP không chính xác"
}
```

## 🎨 GIAO DIỆN

### Màn hình Reset Password bao gồm:

1. **Header** - Icon khóa và tiêu đề
2. **Form nhập email** - TextField để nhập email
3. **Button "Gửi mã OTP"** - Gửi request đến server
4. **Thông báo OTP đã gửi** - Hiện sau khi gửi thành công
5. **Form nhập OTP** - TextField 6 số (hiện sau khi gửi OTP)
6. **Form mật khẩu mới** - TextField password
7. **Form xác nhận mật khẩu** - TextField password
8. **Button "Đổi mật khẩu"** - Submit form
9. **ProgressBar** - Hiển thị khi đang xử lý

## 🧪 TEST CHỨC NĂNG

### Test Case 1: Gửi OTP thành công
1. Nhập email đã đăng ký
2. Click "Gửi mã OTP"
3. Kiểm tra email nhận được OTP
4. Form OTP và password hiện ra

### Test Case 2: Email không tồn tại
1. Nhập email chưa đăng ký
2. Click "Gửi mã OTP"
3. Hiện lỗi: "Email không tồn tại trong hệ thống"

### Test Case 3: Tài khoản Google
1. Nhập email tài khoản Google
2. Click "Gửi mã OTP"
3. Hiện lỗi: "Tài khoản Google không thể đổi mật khẩu..."

### Test Case 4: OTP đúng
1. Gửi OTP thành công
2. Nhập đúng mã OTP từ email
3. Nhập mật khẩu mới hợp lệ
4. Đổi mật khẩu thành công

### Test Case 5: OTP sai
1. Gửi OTP thành công
2. Nhập sai mã OTP
3. Hiện lỗi: "Mã OTP không chính xác"

### Test Case 6: OTP hết hạn
1. Gửi OTP
2. Đợi hơn 5 phút
3. Nhập OTP
4. Hiện lỗi: "Mã OTP đã hết hạn..."

### Test Case 7: Mật khẩu không khớp
1. Gửi OTP và nhập đúng
2. Nhập mật khẩu mới
3. Xác nhận mật khẩu khác với mật khẩu mới
4. Hiện lỗi: "Mật khẩu xác nhận không khớp"

## 📞 HỖ TRỢ

Nếu gặp vấn đề, kiểm tra:
1. Log trong `Server/` bằng cách xem file error_log
2. Response từ API trong Logcat của Android Studio
3. Email settings trong Gmail

## ✅ CHECKLIST TRIỂN KHAI

- [ ] Chạy SQL script để thêm cột vào database
- [ ] Cài đặt PHPMailer
- [ ] Cấu hình email trong `email_config.php`
- [ ] Upload files PHP lên server
- [ ] Test gửi email OTP
- [ ] Test flow đổi mật khẩu hoàn chỉnh
- [ ] Test với tài khoản Google (phải báo lỗi)
- [ ] Test OTP hết hạn
- [ ] Test validation form

---

**Tác giả:** Duy Trường  
**Ngày tạo:** 27/10/2025  
**Phiên bản:** 1.0

