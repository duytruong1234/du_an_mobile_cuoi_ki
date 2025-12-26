# HƯỚNG DẪN COPY FILES PAYPAL VÀO XAMPP

## 🎯 VẤN ĐỀ

Bạn đang dùng XAMPP với web root: `C:\xampp\htdocs\appbandienthoai`

Các file PayPal đang ở: `D:\AppBanDongHo\Server\`

Cần copy sang: `C:\xampp\htdocs\appbandienthoai\Server\`

---

## ✅ GIẢI PHÁP NHANH - CÁCH 1 (TỰ ĐỘNG)

### Bước 1: Chạy File Batch

1. Vào thư mục `D:\AppBanDongHo\`
2. **Double-click** file: `copy_paypal_to_xampp.bat`
3. Đợi copy xong
4. Nhấn phím bất kỳ để đóng

### Bước 2: Test

Mở trình duyệt:
```
http://localhost/appbandienthoai/Server/test_paypal.php
```

**Kết quả mong đợi:**
```json
{
  "files": {
    "paypal_config.php": "EXISTS",
    "paypal_create_payment.php": "EXISTS",
    ...
  }
}
```

---

## 📝 GIẢI PHÁP THỦ CÔNG - CÁCH 2

Nếu batch không chạy, làm thủ công:

### Bước 1: Tạo thư mục Server

1. Mở File Explorer
2. Vào: `C:\xampp\htdocs\appbandienthoai\`
3. Tạo thư mục mới tên: `Server` (nếu chưa có)

### Bước 2: Copy 7 files

Copy **7 files** này từ `D:\AppBanDongHo\Server\`:

1. `paypal_config.php`
2. `paypal_create_payment.php`
3. `paypal_execute_payment.php`
4. `paypal_check_status.php`
5. `paypal_return.php`
6. `paypal_cancel.php`
7. `test_paypal.php`

Paste vào: `C:\xampp\htdocs\appbandienthoai\Server\`

### Bước 3: Test

Mở trình duyệt:
```
http://localhost/appbandienthoai/Server/test_paypal.php
```

---

## 🔧 SAU KHI COPY

### 1. Cấu hình PayPal Credentials

Mở file: `C:\xampp\htdocs\appbandienthoai\Server\paypal_config.php`

Sửa dòng 11-12:
```php
define('PAYPAL_CLIENT_ID', 'YOUR_SANDBOX_CLIENT_ID');
define('PAYPAL_CLIENT_SECRET', 'YOUR_SANDBOX_CLIENT_SECRET');
```

Thay bằng credentials thật từ: https://developer.paypal.com/

### 2. Cập nhật Return URL

Dòng 27-28:
```php
define('PAYPAL_RETURN_URL', 'http://localhost/appbandienthoai/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'http://localhost/appbandienthoai/Server/paypal_cancel.php');
```

### 3. Cập nhật Utils.java trong Android

Mở: `app/src/main/java/.../utils/Utils.java`

Sửa:
```java
public static String BASE_URL = "http://10.0.2.2/appbandienthoai/";
```

**Lưu ý:**
- `10.0.2.2` = localhost từ Android Emulator
- Nếu dùng thiết bị thật: dùng IP máy tính (vd: `http://192.168.1.100/appbandienthoai/`)

---

## ✅ CHECKLIST

- [ ] Copy 7 files PayPal vào `C:\xampp\htdocs\appbandienthoai\Server\`
- [ ] Test `test_paypal.php` thấy "EXISTS"
- [ ] Cập nhật `paypal_config.php` với Client ID & Secret
- [ ] Cập nhật Return URL trong config
- [ ] Cập nhật BASE_URL trong Utils.java
- [ ] Rebuild Android app
- [ ] Test thanh toán PayPal

---

## 🚀 TEST APP

Sau khi làm xong:

1. **Rebuild app** trong Android Studio
2. **Chạy app**
3. **Thêm sản phẩm** vào giỏ
4. **Click "Thanh toán"**
5. **Click "THANH TOÁN PAYPAL"**

Log sẽ hiện:
```
D/PayPal: ===> Nút PayPal được bấm <===
D/PayPal: Chuẩn bị gọi API createPayPalPayment
```

Nếu thấy log trên → ✅ THÀNH CÔNG!

---

## 📞 LƯU Ý

**Mỗi lần sửa file PHP trong `D:\AppBanDongHo\Server\`:**

Phải chạy lại `copy_paypal_to_xampp.bat` hoặc copy thủ công sang XAMPP!

Vì web server đọc từ `C:\xampp\htdocs\`, không phải từ `D:\AppBanDongHo\`.
@echo off
echo ========================================
echo COPY PAYPAL FILES TO XAMPP
echo ========================================
echo.

REM Tao thu muc Server neu chua co
if not exist "C:\xampp\htdocs\appbandienthoai\Server" (
    echo Creating Server directory...
    mkdir "C:\xampp\htdocs\appbandienthoai\Server"
)

echo Copying PayPal files...
echo.

copy /Y "D:\AppBanDongHo\Server\paypal_config.php" "C:\xampp\htdocs\appbandienthoai\Server\"
echo [OK] paypal_config.php

copy /Y "D:\AppBanDongHo\Server\paypal_create_payment.php" "C:\xampp\htdocs\appbandienthoai\Server\"
echo [OK] paypal_create_payment.php

copy /Y "D:\AppBanDongHo\Server\paypal_execute_payment.php" "C:\xampp\htdocs\appbandienthoai\Server\"
echo [OK] paypal_execute_payment.php

copy /Y "D:\AppBanDongHo\Server\paypal_check_status.php" "C:\xampp\htdocs\appbandienthoai\Server\"
echo [OK] paypal_check_status.php

copy /Y "D:\AppBanDongHo\Server\paypal_return.php" "C:\xampp\htdocs\appbandienthoai\Server\"
echo [OK] paypal_return.php

copy /Y "D:\AppBanDongHo\Server\paypal_cancel.php" "C:\xampp\htdocs\appbandienthoai\Server\"
echo [OK] paypal_cancel.php

copy /Y "D:\AppBanDongHo\Server\test_paypal.php" "C:\xampp\htdocs\appbandienthoai\Server\"
echo [OK] test_paypal.php

echo.
echo ========================================
echo DONE! All PayPal files copied successfully!
echo ========================================
echo.
echo Now open browser and test:
echo http://localhost/appbandienthoai/Server/test_paypal.php
echo.
pause

