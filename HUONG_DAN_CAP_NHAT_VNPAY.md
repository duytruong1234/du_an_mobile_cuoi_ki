# HƯỚNG DẪN CẬP NHẬT THÔNG TIN VNPAY

## 🚨 Vấn đề hiện tại
Lỗi "Không tìm thấy website" xuất hiện vì tài khoản VNPay sandbox demo đã bị vô hiệu hóa.

## ✅ Giải pháp

### Bước 1: Đăng ký tài khoản VNPay Sandbox
1. Truy cập: **https://sandbox.vnpayment.vn/devreg/**
2. Điền đầy đủ thông tin:
   - Email
   - Số điện thoại  
   - Tên website: `App Bán Đồng Hồ`
   - Return URL: `https://your-ngrok-url.ngrok-free.dev/appbandienthoai/vnpay_return.php`
3. Submit form

### Bước 2: Nhận email xác nhận
VNPay sẽ gửi email chứa:
- **vnp_TmnCode** (Mã Website)
- **vnp_HashSecret** (Chuỗi bí mật)

### Bước 3: Cập nhật code

#### 📄 File 1: `Server/vnpay_config.php`
```php
// Thay thế dòng này:
define('VNPAY_TMN_CODE', 'DEMOSHOP');
// Bằng:
define('VNPAY_TMN_CODE', 'MÃ_BẠN_NHẬN_ĐƯỢC');

// Thay thế dòng này:
define('VNPAY_HASH_SECRET', 'RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ');
// Bằng:
define('VNPAY_HASH_SECRET', 'CHUỖI_BÍ_MẬT_BẠN_NHẬN_ĐƯỢC');
```

#### 📄 File 2: `app/src/main/java/vn/duytruong/appbandienthoai/model/VNPayConfig.java`
```java
// Thay thế:
public static final String VNPAY_TMN_CODE = "DEMOSHOP";
// Bằng:
public static final String VNPAY_TMN_CODE = "MÃ_BẠN_NHẬN_ĐƯỢC";

// Thay thế:
public static final String VNPAY_HASH_SECRET = "RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ";
// Bằng:
public static final String VNPAY_HASH_SECRET = "CHUỖI_BÍ_MẬT_BẠN_NHẬN_ĐƯỢC";
```

### Bước 4: Build lại app
1. Trong Android Studio: **Build → Rebuild Project**
2. Chạy lại app và thử thanh toán

---

## 🔍 Lưu ý về Return URL

Nếu bạn dùng **Android Emulator**:
- Dùng: `http://10.0.2.2/appbandienthoai/vnpay_return.php`

Nếu bạn dùng **điện thoại thật**:
- Phải dùng ngrok: `https://your-url.ngrok-free.dev/appbandienthoai/vnpay_return.php`

---

## 📞 Hỗ trợ

Nếu gặp khó khăn khi đăng ký VNPay Sandbox, liên hệ:
- Email: support@vnpay.vn
- Hotline: 1900 55 55 77

---

**Lưu ý:** Tài khoản sandbox được kích hoạt tự động trong vài phút sau khi đăng ký.

