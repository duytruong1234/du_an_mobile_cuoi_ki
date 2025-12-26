
# HƯỚNG DẪN TÍCH HỢP THANH TOÁN PAYPAL

## 📋 Mục lục
1. [Giới thiệu](#giới-thiệu)
2. [Cấu hình PayPal](#cấu-hình-paypal)
3. [Cập nhật Database](#cập-nhật-database)
4. [Cấu hình Server](#cấu-hình-server)
5. [Chạy ứng dụng](#chạy-ứng-dụng)
6. [Test thanh toán](#test-thanh-toán)

---

## 🎯 Giới thiệu

Hệ thống đã được tích hợp thanh toán PayPal, cho phép khách hàng thanh toán đơn hàng qua PayPal.

### Tính năng:
- ✅ Tạo đơn hàng và chuyển sang PayPal để thanh toán
- ✅ Tự động chuyển đổi VND sang USD
- ✅ Xác nhận thanh toán tự động
- ✅ Deep link để quay lại app sau khi thanh toán
- ✅ Hỗ trợ cả môi trường Sandbox (test) và Live (production)

---

## 🔧 Cấu hình PayPal

### Bước 1: Tạo PayPal Developer Account

1. Truy cập: https://developer.paypal.com/
2. Đăng nhập hoặc tạo tài khoản mới
3. Vào **Dashboard** > **Apps & Credentials**

### Bước 2: Tạo App để lấy API Credentials

1. Chọn tab **Sandbox** (để test) hoặc **Live** (production)
2. Click **Create App**
3. Nhập tên app (vd: "Shop Dong Ho")
4. Chọn **Merchant** làm app type
5. Click **Create App**

### Bước 3: Lấy Client ID và Secret

Sau khi tạo app, bạn sẽ thấy:
- **Client ID**: `AxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxE`
- **Secret**: `ExxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxM`

**Lưu ý**: Giữ Secret bảo mật, không public lên Github!

### Bước 4: Tạo Sandbox Test Accounts

1. Vào **Sandbox** > **Accounts**
2. PayPal tự động tạo 2 accounts:
   - **Business Account**: Nhận tiền (merchant)
   - **Personal Account**: Trả tiền (buyer)
3. Click vào từng account để xem email và password
4. Dùng Personal Account để test thanh toán

---

## 💾 Cập nhật Database

### Chạy file SQL

Chạy file `update_database_paypal.sql` để thêm các cột PayPal vào bảng `donhang`:

```sql
-- Mở phpMyAdmin hoặc MySQL client
-- Chọn database của bạn
-- Import hoặc chạy file: update_database_paypal.sql
```

File này sẽ thêm:
- `paypal_order_id`: Lưu PayPal Order ID
- `paypal_payer_id`: Lưu PayPal Payer ID
- `paypal_payment_date`: Thời gian thanh toán

---

## ⚙️ Cấu hình Server

### 1. Cập nhật file `Server/paypal_config.php`

Mở file và cập nhật các thông tin sau:

```php
// Thay YOUR_SANDBOX_CLIENT_ID bằng Client ID từ PayPal Developer
define('PAYPAL_CLIENT_ID', 'AxxxxxxxxxxxxxxxxxxxxxxxxxxxE');

// Thay YOUR_SANDBOX_CLIENT_SECRET bằng Secret từ PayPal Developer
define('PAYPAL_CLIENT_SECRET', 'ExxxxxxxxxxxxxxxxxxxxxxxxxxxM');

// Cập nhật Return URL (thay your-domain.com bằng domain thực của bạn)
define('PAYPAL_RETURN_URL', 'https://your-domain.com/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://your-domain.com/Server/paypal_cancel.php');

// Tỷ giá VND -> USD (cập nhật theo tỷ giá thực)
define('VND_TO_USD_RATE', 24000); // 1 USD = 24,000 VND
```

### 2. Sử dụng ngrok (nếu test local)

Nếu bạn đang chạy server local, cần dùng ngrok:

```bash
ngrok http 80
```

Sau đó cập nhật URL trong `paypal_config.php`:

```php
define('PAYPAL_RETURN_URL', 'https://your-ngrok-url.ngrok-free.app/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://your-ngrok-url.ngrok-free.app/Server/paypal_cancel.php');
```

### 3. Cập nhật Utils.java

Mở file `app/src/main/java/vn/duytruong/appbandienthoai/utils/Utils.java`:

```java
// Cập nhật BASE_URL
public static String BASE_URL = "https://your-ngrok-url.ngrok-free.app/";
```

---

## 🚀 Chạy ứng dụng

### 1. Sync Gradle

```bash
# Android Studio sẽ tự động download PayPal SDK
# Hoặc chạy lệnh:
./gradlew build
```

### 2. Build và chạy app

1. Connect thiết bị Android hoặc khởi động emulator
2. Click **Run** trong Android Studio
3. Đăng nhập vào app

---

## 🧪 Test thanh toán PayPal

### Bước 1: Thêm sản phẩm vào giỏ hàng

1. Mở app
2. Chọn sản phẩm và thêm vào giỏ hàng
3. Vào giỏ hàng, click **Thanh toán**

### Bước 2: Chọn thanh toán PayPal

1. Nhập địa chỉ giao hàng
2. Click nút **THANH TOÁN PAYPAL** (màu xanh #0070BA)
3. App sẽ mở trình duyệt và chuyển đến PayPal

### Bước 3: Đăng nhập PayPal Sandbox

Trên trang PayPal, đăng nhập bằng **Sandbox Personal Account**:

```
Email: sb-xxxxx@personal.example.com
Password: (xem trong PayPal Developer Dashboard)
```

### Bước 4: Xác nhận thanh toán

1. Kiểm tra thông tin đơn hàng
2. Click **Pay Now**
3. PayPal sẽ redirect về app
4. App tự động xác nhận thanh toán và hiển thị đơn hàng

### Bước 5: Kiểm tra kết quả

1. Vào **Xem đơn hàng** để xem trạng thái
2. Trạng thái sẽ là **Đang giao hàng** nếu thanh toán thành công
3. Kiểm tra database - bảng `donhang` sẽ có thông tin PayPal

---

## 📊 Quy trình thanh toán

```
1. User click "THANH TOÁN PAYPAL"
   ↓
2. App gọi API: paypal_create_payment.php
   ↓
3. Server tạo đơn hàng trong DB
   ↓
4. Server gọi PayPal API để tạo Order
   ↓
5. Server trả về approval_url
   ↓
6. App mở trình duyệt với approval_url
   ↓
7. User đăng nhập PayPal và thanh toán
   ↓
8. PayPal redirect về: paypal_return.php
   ↓
9. paypal_return.php redirect về app (deep link)
   ↓
10. App gọi API: paypal_execute_payment.php
   ↓
11. Server gọi PayPal API để capture payment
   ↓
12. Server cập nhật trạng thái đơn hàng
   ↓
13. App hiển thị kết quả thanh toán
```

---

## 🔍 Troubleshooting

### Lỗi: "Không thể kết nối với PayPal"

**Nguyên nhân**: Client ID hoặc Secret sai

**Giải pháp**:
1. Kiểm tra lại `paypal_config.php`
2. Copy đúng Client ID và Secret từ PayPal Dashboard
3. Đảm bảo không có khoảng trắng thừa

### Lỗi: "Link thanh toán không hợp lệ"

**Nguyên nhân**: Server không trả về approval_url

**Giải pháp**:
1. Kiểm tra log trong Android Studio: filter "PayPal"
2. Kiểm tra response từ server
3. Đảm bảo file PHP không có lỗi syntax

### Lỗi: "Không quay lại app sau thanh toán"

**Nguyên nhân**: Deep link không hoạt động

**Giải pháp**:
1. Kiểm tra AndroidManifest.xml đã có intent-filter cho PayPal
2. Kiểm tra scheme: `appbandienthoai://payment/paypal`
3. Rebuild app sau khi sửa AndroidManifest.xml

### Thanh toán thành công nhưng trạng thái không cập nhật

**Nguyên nhân**: API execute_payment không được gọi

**Giải pháp**:
1. Kiểm tra onNewIntent trong ThanhToanActivity
2. Xem log để đảm bảo API được gọi
3. Kiểm tra database có cột paypal_* chưa

---

## 🌐 Chuyển sang Production

Khi ready để sử dụng thực tế:

### 1. Cập nhật paypal_config.php

```php
// Đổi sang Live mode
define('PAYPAL_MODE', 'live');

// Sử dụng Live credentials
define('PAYPAL_CLIENT_ID', 'YOUR_LIVE_CLIENT_ID');
define('PAYPAL_CLIENT_SECRET', 'YOUR_LIVE_CLIENT_SECRET');
```

### 2. Cập nhật Return URL

```php
// Sử dụng domain thực
define('PAYPAL_RETURN_URL', 'https://your-real-domain.com/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://your-real-domain.com/Server/paypal_cancel.php');
```

### 3. Test kỹ trước khi deploy

- Test với số tiền nhỏ
- Kiểm tra toàn bộ flow
- Đảm bảo đơn hàng được cập nhật đúng

---

## 📝 Lưu ý quan trọng

1. **Bảo mật**: Không public Client Secret lên Github
2. **Tỷ giá**: Cập nhật tỷ giá VND/USD thường xuyên
3. **Phí PayPal**: PayPal tính phí ~4.4% + $0.30 mỗi giao dịch
4. **Currency**: PayPal hỗ trợ nhiều loại tiền tệ, mặc định là USD
5. **Webhook**: Nên setup webhook để nhận thông báo từ PayPal

---

## 🎨 Giao diện

App đã có nút **THANH TOÁN PAYPAL** màu xanh (#0070BA) với icon PayPal, nằm dưới nút VNPay.

---

## 📞 Hỗ trợ

Nếu gặp vấn đề:
1. Xem log trong Android Studio (filter: "PayPal")
2. Kiểm tra PayPal Developer Dashboard > Activity
3. Xem file log của PHP server

---

**Chúc bạn tích hợp thành công! 🎉**

