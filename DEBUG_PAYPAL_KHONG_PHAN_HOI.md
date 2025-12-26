# DEBUG: PAYPAL KHÔNG PHẢN HỒI - HƯỚNG DẪN KIỂM TRA

## 🔍 KIỂM TRA NHANH

### Bước 1: Kiểm tra nút PayPal có hoạt động không

1. **Chạy app** và mở màn hình Thanh toán
2. **Mở Logcat** trong Android Studio
3. **Filter**: Nhập "PayPal" 
4. **Click nút "THANH TOÁN PAYPAL"**

**Kết quả mong đợi:**
```
D/PayPal: ===> Nút PayPal được bấm <===
D/PayPal: Chuẩn bị gọi API createPayPalPayment
D/PayPal: Dữ liệu gửi: iduser=...
```

**Nếu KHÔNG thấy log:**
- ❌ Nút chưa được khởi tạo hoặc bị null
- ✅ Giải pháp: Xem bước 2

**Nếu thấy log nhưng KHÔNG có response:**
- ❌ API không gọi được hoặc file PHP không tồn tại
- ✅ Giải pháp: Xem bước 3

---

### Bước 2: Kiểm tra nút PayPal có tồn tại trong layout không

**Kiểm tra trong Logcat khi mở màn hình:**

```
D/PayPal: initView: btnPayPal tìm thấy và bật click OK
```

**Nếu thấy:**
```
E/PayPal: initView: btnPayPal là NULL — kiểm tra ID trong XML layout!
```

**Giải pháp:**
1. Mở file `app/src/main/res/layout/activity_dat_hang.xml`
2. Tìm button PayPal, đảm bảo có ID: `android:id="@+id/btnPayPal"`
3. Rebuild app

---

### Bước 3: Test API PayPal có hoạt động không

**Mở trình duyệt và truy cập:**

```
http://YOUR_SERVER_URL/Server/test_paypal.php
```

**Kết quả mong đợi:**
```json
{
  "test": "PayPal API Test",
  "files": {
    "paypal_config.php": "EXISTS",
    "paypal_create_payment.php": "EXISTS",
    "paypal_execute_payment.php": "EXISTS",
    "paypal_check_status.php": "EXISTS",
    "paypal_return.php": "EXISTS",
    "paypal_cancel.php": "EXISTS"
  },
  "server_info": {
    "curl_enabled": "YES"
  }
}
```

**Nếu file nào "NOT FOUND":**
- ❌ File chưa được upload lên server
- ✅ Upload file từ `D:\AppBanDongHo\Server\` lên web server

**Nếu curl_enabled: "NO":**
- ❌ Server không hỗ trợ cURL (cần thiết cho PayPal API)
- ✅ Liên hệ hosting để enable cURL extension

---

### Bước 4: Test API create payment trực tiếp

**Dùng Postman hoặc trình duyệt:**

**URL:** `http://YOUR_SERVER_URL/Server/paypal_create_payment.php`

**Method:** POST

**Body (form-data):**
```
iduser: 1
diachi: 123 Test Street
sodienthoai: 0123456789
soluong: 1
tongtien: 100000
cartItems: [{"id":1,"name":"Test","price":100000}]
order_info: Test order
```

**Kết quả mong đợi (nếu cấu hình đúng):**
```json
{
  "success": true,
  "message": "Tạo đơn hàng PayPal thành công",
  "approval_url": "https://www.sandbox.paypal.com/...",
  "madonhang": "DH123456789"
}
```

**Nếu lỗi:**
```json
{
  "success": false,
  "message": "Không thể kết nối với PayPal"
}
```

**Nguyên nhân:**
- ❌ `paypal_config.php` chưa có Client ID và Secret
- ✅ Xem bước 5

---

### Bước 5: Cấu hình PayPal Credentials

**Mở file:** `Server/paypal_config.php`

**Kiểm tra dòng 11-12:**
```php
define('PAYPAL_CLIENT_ID', 'YOUR_SANDBOX_CLIENT_ID');  // ← CẦN THAY
define('PAYPAL_CLIENT_SECRET', 'YOUR_SANDBOX_CLIENT_SECRET');  // ← CẦN THAY
```

**Nếu vẫn là "YOUR_SANDBOX_...":**
- ❌ Chưa cấu hình credentials
- ✅ Làm theo hướng dẫn bên dưới

---

## 📝 HƯỚNG DẪN LẤY PAYPAL CREDENTIALS

### 1. Đăng ký PayPal Developer

1. Truy cập: https://developer.paypal.com/
2. Đăng nhập bằng tài khoản PayPal (hoặc tạo mới)
3. Click **Dashboard**

### 2. Tạo App

1. Vào **Apps & Credentials**
2. Chọn tab **Sandbox** (để test)
3. Click **Create App**
4. Điền:
   - **App Name:** Shop Dong Ho (hoặc tên bất kỳ)
   - **App Type:** Merchant
5. Click **Create App**

### 3. Copy Credentials

Sau khi tạo app, bạn sẽ thấy:

```
Client ID: AZaQ7FLpuK9X... (dài ~80 ký tự)
Secret: EKfN8... (click "Show" để xem, dài ~80 ký tự)
```

### 4. Cập nhật paypal_config.php

```php
define('PAYPAL_CLIENT_ID', 'AZaQ7FLpuK9X...');  // Paste Client ID
define('PAYPAL_CLIENT_SECRET', 'EKfN8...');      // Paste Secret
```

### 5. Cập nhật Return URL

**Nếu dùng ngrok:**
```php
define('PAYPAL_RETURN_URL', 'https://abc123.ngrok-free.app/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://abc123.ngrok-free.app/Server/paypal_cancel.php');
```

**Nếu có domain:**
```php
define('PAYPAL_RETURN_URL', 'https://yourdomain.com/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://yourdomain.com/Server/paypal_cancel.php');
```

---

## ✅ CHECKLIST ĐẦY ĐỦ

- [ ] File `paypal_create_payment.php` tồn tại trong Server/
- [ ] File `paypal_execute_payment.php` tồn tại trong Server/
- [ ] File `paypal_check_status.php` tồn tại trong Server/
- [ ] File `paypal_return.php` tồn tại trong Server/
- [ ] File `paypal_cancel.php` tồn tại trong Server/
- [ ] `paypal_config.php` có Client ID thật (không phải "YOUR_SANDBOX_...")
- [ ] `paypal_config.php` có Secret thật
- [ ] Return URL đúng với server của bạn
- [ ] Database có cột `paypal_order_id`, `paypal_payer_id`, `paypal_payment_date`
- [ ] cURL enabled trên server
- [ ] Button PayPal có ID `btnPayPal` trong XML
- [ ] Rebuild app sau khi sửa code

---

## 🔧 GIẢI PHÁP NHANH NHẤT

**Chạy lệnh SQL này trong phpMyAdmin:**

```sql
-- Thêm cột PayPal vào bảng donhang
ALTER TABLE `donhang` 
ADD COLUMN `paypal_order_id` VARCHAR(100) NULL,
ADD COLUMN `paypal_payer_id` VARCHAR(100) NULL,
ADD COLUMN `paypal_payment_date` DATETIME NULL;
```

**Cập nhật paypal_config.php:**

1. Lấy credentials từ https://developer.paypal.com/
2. Paste vào file `paypal_config.php`
3. Cập nhật Return URL

**Rebuild app:**

1. Android Studio > Build > Clean Project
2. Build > Rebuild Project
3. Run app

---

## 📞 VẪN KHÔNG HOẠT ĐỘNG?

**Gửi cho tôi log từ Logcat:**

1. Mở Logcat
2. Filter: "PayPal"
3. Click nút PayPal
4. Copy toàn bộ log và gửi cho tôi

**Hoặc test API bằng URL:**

```
http://YOUR_SERVER/Server/test_paypal.php
```

Gửi kết quả JSON cho tôi để debug!

