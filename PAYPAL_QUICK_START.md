# TÍCH HỢP PAYPAL - TÓM TẮT NHANH

## ✅ Đã hoàn thành

### 1. Android App
- ✅ Thêm PayPal SDK vào `build.gradle.kts`
- ✅ Tạo model: `PayPalResponse.java`, `PayPalStatusResponse.java`
- ✅ Cập nhật API endpoints trong `ApiBanHang.java`
- ✅ Thêm nút PayPal vào giao diện `activity_dat_hang.xml`
- ✅ Tạo icon PayPal: `ic_paypal_logo.xml`
- ✅ Cập nhật `ThanhToanActivity.java` để xử lý thanh toán PayPal
- ✅ Thêm deep link trong `AndroidManifest.xml`

### 2. Server PHP
- ✅ `paypal_config.php` - Cấu hình PayPal credentials
- ✅ `paypal_create_payment.php` - Tạo đơn hàng và PayPal order
- ✅ `paypal_execute_payment.php` - Xác nhận thanh toán
- ✅ `paypal_check_status.php` - Kiểm tra trạng thái đơn hàng
- ✅ `paypal_return.php` - Xử lý callback khi thanh toán thành công
- ✅ `paypal_cancel.php` - Xử lý khi user hủy thanh toán

### 3. Database
- ✅ `update_database_paypal.sql` - Script thêm cột PayPal vào bảng donhang

### 4. Tài liệu
- ✅ `HUONG_DAN_PAYPAL.md` - Hướng dẫn chi tiết

---

## 🚀 BƯỚC TIẾP THEO - LÀM NGAY

### Bước 1: Cập nhật Database (2 phút)

```bash
# Mở phpMyAdmin hoặc MySQL client
# Chạy file: update_database_paypal.sql
```

Hoặc chạy trực tiếp SQL:
```sql
ALTER TABLE `donhang` 
ADD COLUMN `paypal_order_id` VARCHAR(100) NULL,
ADD COLUMN `paypal_payer_id` VARCHAR(100) NULL,
ADD COLUMN `paypal_payment_date` DATETIME NULL,
ADD INDEX `idx_paypal_order_id` (`paypal_order_id`);
```

### Bước 2: Cấu hình PayPal (5 phút)

1. **Đăng ký PayPal Developer:**
   - Truy cập: https://developer.paypal.com/
   - Đăng nhập hoặc tạo tài khoản

2. **Tạo App:**
   - Dashboard > Apps & Credentials > Create App
   - Tên: "Shop Dong Ho"
   - Chọn Sandbox

3. **Lấy Credentials:**
   - Copy **Client ID**
   - Copy **Secret**

4. **Cập nhật `Server/paypal_config.php`:**
   ```php
   define('PAYPAL_CLIENT_ID', 'PASTE_CLIENT_ID_HERE');
   define('PAYPAL_CLIENT_SECRET', 'PASTE_SECRET_HERE');
   ```

### Bước 3: Cấu hình Server URL (3 phút)

**Nếu dùng ngrok:**
```bash
# Chạy ngrok
ngrok http 80

# Copy URL (vd: https://abc123.ngrok-free.app)
```

**Cập nhật `paypal_config.php`:**
```php
define('PAYPAL_RETURN_URL', 'https://YOUR-NGROK-URL/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://YOUR-NGROK-URL/Server/paypal_cancel.php');
```

**Cập nhật `Utils.java`:**
```java
public static String BASE_URL = "https://YOUR-NGROK-URL/";
```

### Bước 4: Build & Run (2 phút)

```bash
# Trong Android Studio:
# 1. File > Sync Project with Gradle Files
# 2. Build > Clean Project
# 3. Build > Rebuild Project
# 4. Run
```

---

## 🧪 TEST NHANH

### 1. Lấy Sandbox Test Account

1. Vào https://developer.paypal.com/dashboard/accounts
2. Xem **Personal Account** (để test thanh toán)
3. Copy email và password

### 2. Test Flow

1. **Trong app:**
   - Thêm sản phẩm vào giỏ
   - Vào giỏ hàng > Thanh toán
   - Nhập địa chỉ
   - Click **THANH TOÁN PAYPAL**

2. **Trên PayPal:**
   - Đăng nhập bằng Sandbox Personal Account
   - Click "Pay Now"

3. **Kết quả:**
   - App tự động quay lại
   - Hiển thị "Thanh toán thành công"
   - Đơn hàng có trạng thái "Đang giao hàng"

---

## 📝 CHECKLIST

- [ ] Chạy SQL để cập nhật database
- [ ] Đăng ký PayPal Developer account
- [ ] Tạo app và lấy Client ID + Secret
- [ ] Cập nhật `paypal_config.php` với credentials
- [ ] Cập nhật Return URL trong `paypal_config.php`
- [ ] Cập nhật BASE_URL trong `Utils.java`
- [ ] Sync Gradle trong Android Studio
- [ ] Build và chạy app
- [ ] Test thanh toán với Sandbox account

---

## 🔧 FILES CẦN CHỈNH SỬA

### 1. Server/paypal_config.php
```php
// Dòng 10-11: Thay YOUR_SANDBOX_CLIENT_ID
define('PAYPAL_CLIENT_ID', 'YOUR_SANDBOX_CLIENT_ID');
define('PAYPAL_CLIENT_SECRET', 'YOUR_SANDBOX_CLIENT_SECRET');

// Dòng 26-27: Thay your-domain.com
define('PAYPAL_RETURN_URL', 'https://your-domain.com/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://your-domain.com/Server/paypal_cancel.php');
```

### 2. app/.../utils/Utils.java
```java
// Thay BASE_URL
public static String BASE_URL = "https://your-ngrok-url.ngrok-free.app/";
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Client Secret**: KHÔNG chia sẻ hoặc commit lên Github
2. **Tỷ giá VND/USD**: Mặc định 24,000 VND = 1 USD (có thể thay đổi trong `paypal_config.php`)
3. **Phí PayPal**: ~4.4% + $0.30 mỗi giao dịch
4. **Deep Link**: Đảm bảo rebuild app sau khi sửa AndroidManifest.xml

---

## 🎯 NÚT PAYPAL TRONG APP

Nút PayPal đã được thêm vào màn hình thanh toán:
- **Vị trí**: Dưới nút VNPay
- **Màu**: Xanh dương PayPal (#0070BA)
- **Text**: "THANH TOÁN PAYPAL"
- **Icon**: Logo PayPal trắng

---

## 📞 TROUBLESHOOTING NHANH

**Lỗi build**: Sync Gradle lại
**Nút PayPal không hiện**: Clean & Rebuild Project
**Không mở PayPal**: Kiểm tra `paypal_config.php` và log
**Không quay lại app**: Rebuild app sau khi sửa AndroidManifest.xml

---

**Thời gian ước tính: 15-20 phút để setup và test hoàn chỉnh! 🚀**

