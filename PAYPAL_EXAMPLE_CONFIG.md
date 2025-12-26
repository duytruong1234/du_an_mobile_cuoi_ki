# PAYPAL INTEGRATION - EXAMPLE CONFIG

## File: Server/paypal_config.php

### Ví dụ cấu hình cho môi trường test (Sandbox)

```php
<?php
// Môi trường Sandbox để test
define('PAYPAL_MODE', 'sandbox');

// Credentials từ https://developer.paypal.com/dashboard/applications/sandbox
define('PAYPAL_CLIENT_ID', 'AZaQ7FLpuK9X...');  // Thay bằng Client ID thực
define('PAYPAL_CLIENT_SECRET', 'EKfN8...');      // Thay bằng Secret thực

// PayPal API URL (sandbox)
define('PAYPAL_API_URL', 'https://api-m.sandbox.paypal.com');

// Return URLs - CẬP NHẬT URL NÀY
define('PAYPAL_RETURN_URL', 'https://abc123.ngrok-free.app/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://abc123.ngrok-free.app/Server/paypal_cancel.php');

// Currency & Exchange Rate
define('PAYPAL_CURRENCY', 'USD');
define('VND_TO_USD_RATE', 24000);  // 1 USD = 24,000 VND
?>
```

### Lấy credentials từ đâu?

1. **Đăng nhập**: https://developer.paypal.com/
2. **Vào**: Dashboard > Apps & Credentials
3. **Chọn tab**: Sandbox (để test)
4. **Click**: Create App
   - App Name: "Shop Dong Ho" (hoặc tên bất kỳ)
   - App Type: Merchant
5. **Copy**:
   - Client ID (dài ~80 ký tự)
   - Secret (click Show để xem, dài ~80 ký tự)

### Lấy Sandbox Test Account

1. **Vào**: Dashboard > Sandbox > Accounts
2. **Xem**: Personal Account (buyer account)
3. **Click** vào dấu "..." > View/Edit Account
4. **Copy**: Email và Password
5. **Dùng account này** để test thanh toán trong app

---

## File: app/.../utils/Utils.java

### Cập nhật BASE_URL

```java
public class Utils {
    // Thay URL này bằng ngrok URL hoặc domain thực
    public static String BASE_URL = "https://abc123.ngrok-free.app/";
    
    // ... các constant khác ...
}
```

### Lấy ngrok URL

Nếu server chạy local:

```bash
# Chạy lệnh
ngrok http 80

# Copy URL từ output (ví dụ):
# https://abc123.ngrok-free.app
```

---

## Test Payment Flow

### 1. Thông tin test (Sandbox)

**Business Account** (nhận tiền - merchant):
- Email: sb-xxxxx@business.example.com
- Password: (xem trong PayPal Developer Dashboard)

**Personal Account** (trả tiền - buyer):
- Email: sb-yyyyy@personal.example.com
- Password: (xem trong PayPal Developer Dashboard)

### 2. Quy trình test

```
1. Mở app > Thêm sản phẩm vào giỏ
   ↓
2. Vào Giỏ hàng > Click "Thanh toán"
   ↓
3. Nhập địa chỉ giao hàng
   ↓
4. Click nút "THANH TOÁN PAYPAL" (màu xanh #0070BA)
   ↓
5. App mở browser > Trang PayPal hiện ra
   ↓
6. Đăng nhập bằng Personal Account (buyer)
   ↓
7. Click "Pay Now"
   ↓
8. PayPal redirect về app
   ↓
9. App hiển thị "Thanh toán thành công"
   ↓
10. Vào "Xem đơn hàng" > Trạng thái: "Đang giao hàng"
```

### 3. Kiểm tra trong PayPal Dashboard

1. Đăng nhập: https://developer.paypal.com/
2. Vào: Sandbox > Accounts
3. Click vào Business Account > Login to Dashboard
4. Xem: Recent Activity > Thấy giao dịch vừa test

---

## Chuyển sang Production (Live)

Khi đã test OK và muốn nhận tiền thật:

### 1. Cập nhật paypal_config.php

```php
// Đổi mode
define('PAYPAL_MODE', 'live');

// Dùng Live credentials (lấy từ tab "Live" trong PayPal Dashboard)
define('PAYPAL_CLIENT_ID', 'YOUR_LIVE_CLIENT_ID');
define('PAYPAL_CLIENT_SECRET', 'YOUR_LIVE_CLIENT_SECRET');

// API URL production
define('PAYPAL_API_URL', 'https://api-m.paypal.com');

// Domain thực (không dùng ngrok)
define('PAYPAL_RETURN_URL', 'https://yourdomain.com/Server/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://yourdomain.com/Server/paypal_cancel.php');
```

### 2. Cập nhật Utils.java

```java
public static String BASE_URL = "https://yourdomain.com/";
```

### 3. Test với số tiền nhỏ trước

- Test với đơn hàng 1 USD
- Kiểm tra đơn hàng được tạo đúng
- Kiểm tra trạng thái cập nhật đúng

---

## Ví dụ test case

### Test Case 1: Thanh toán thành công

**Input:**
- Sản phẩm: Đồng hồ Rolex
- Giá: 500,000 VND
- Số lượng: 2
- Tổng: 1,000,000 VND = ~41.67 USD

**Expected:**
- PayPal hiển thị: $41.67 USD
- Sau thanh toán: Trạng thái "Đang giao hàng"
- Database: paypal_order_id, paypal_payer_id được lưu

### Test Case 2: Hủy thanh toán

**Input:**
- Tạo đơn hàng
- Vào PayPal
- Click "Cancel and return"

**Expected:**
- Quay lại app
- Trạng thái: "Đã hủy"
- Có thể thử thanh toán lại

---

## Common Issues

### 1. "Không thể kết nối PayPal"

**Fix:**
```php
// Kiểm tra paypal_config.php
// Đảm bảo Client ID và Secret đúng
// Không có khoảng trắng thừa
define('PAYPAL_CLIENT_ID', 'ABC123');  // ✅ Đúng
define('PAYPAL_CLIENT_ID', ' ABC123'); // ❌ Sai (có space)
```

### 2. "Link thanh toán không hợp lệ"

**Fix:**
```php
// Kiểm tra PHP error log
// Đảm bảo curl được enable
// Test API bằng Postman
```

### 3. "Không quay lại app"

**Fix:**
```xml
<!-- AndroidManifest.xml phải có intent-filter -->
<!-- Rebuild app sau khi sửa -->
```

---

**Happy coding! 🎉**

