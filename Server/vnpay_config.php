
// Cấu hình thông tin Merchant VNPay
// ✅ Tài khoản VNPay Sandbox chính thức (đã đăng ký ngày 31/10/2025)
if (!defined('VNPAY_TMN_CODE'))
    define('VNPAY_TMN_CODE', '1C93XWXU'); // Mã Website của bạn

if (!defined('VNPAY_HASH_SECRET'))
    define('VNPAY_HASH_SECRET', 'GE4QRFHMNXHQR9XDR7VIY8HSCCSAHYDM'); // Chuỗi bí mật của bạn

if (!defined('VNPAY_URL'))
    define('VNPAY_URL', 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html'); // URL sandbox VNPay

// ✅ RETURN URL - Phải khớp với VNPayConfig.java
if (!defined('VNPAY_RETURN_URL'))
    define('VNPAY_RETURN_URL', 'https://donya-barwise-subversively.ngrok-free.dev/appbandienthoai/vnpay_return.php');

// Timezone
date_default_timezone_set('Asia/Ho_Chi_Minh');

