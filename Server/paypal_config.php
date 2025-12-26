<?php
// File cấu hình PayPal
// PayPal SDK Configuration

// Môi trường: 'sandbox' cho test, 'live' cho production
define('PAYPAL_MODE', 'sandbox');

// PayPal API credentials
// Lấy từ: https://developer.paypal.com/dashboard/applications/sandbox
if (PAYPAL_MODE == 'sandbox') {
    define('PAYPAL_CLIENT_ID', 'Aemg-YrQynLqDLj-jeKSYYOPfU2lPOEFv6jVE6TekgwHPDpGPKB4kJMPazGBc36tmy854ObHvEcztSBC');
    define('PAYPAL_CLIENT_SECRET', 'EBYmM5dNbIyQGsLL76zcYhvUWBCXb7qxPxW5RD5g23GkhCXZ-paBsm5Jjdrb9Na_mbRcbIDCy3JHNp4F');
} else {
    define('PAYPAL_CLIENT_ID', 'Aemg-YrQynLqDLj-jeKSYYOPfU2lPOEFv6jVE6TekgwHPDpGPKB4kJMPazGBc36tmy854ObHvEcztSBC');
    define('PAYPAL_CLIENT_SECRET', 'EBYmM5dNbIyQGsLL76zcYhvUWBCXb7qxPxW5RD5g23GkhCXZ-paBsm5Jjdrb9Na_mbRcbIDCy3JHNp4F');
}

// PayPal API URLs
if (PAYPAL_MODE == 'sandbox') {
    define('PAYPAL_API_URL', 'https://api-m.sandbox.paypal.com');
} else {
    define('PAYPAL_API_URL', 'https://api-m.paypal.com');
}

// Return URL sau khi thanh toán
// ⚠️ QUAN TRỌNG: Cập nhật NGROK_URL mỗi khi restart ngrok!
// Chạy: ngrok http 80
// Sau đó thay thế URL dưới đây bằng URL mới từ ngrok
define('NGROK_URL', 'https://donya-barwise-subversively.ngrok-free.dev'); // 👈 CẬP NHẬT URL NÀY!

define('PAYPAL_RETURN_URL', NGROK_URL . '/appbandienthoai/paypal_return.php');
define('PAYPAL_CANCEL_URL', NGROK_URL . '/appbandienthoai/paypal_cancel.php');

// Currency
define('PAYPAL_CURRENCY', 'USD');

// Tỷ giá VND -> USD (cập nhật theo tỷ giá thực tế)
define('VND_TO_USD_RATE', 24000);
// Không có ?> ở cuối để tránh whitespace

