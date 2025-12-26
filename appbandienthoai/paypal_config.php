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

define('PAYPAL_RETURN_URL', 'https://donya-barwise-subversively.ngrok-free.dev/appbandienthoai/paypal_return.php');
define('PAYPAL_CANCEL_URL', 'https://donya-barwise-subversively.ngrok-free.dev/appbandienthoai/paypal_cancel.php');

// Currency
define('PAYPAL_CURRENCY', 'USD');

// Tỷ giá VND -> USD (cập nhật theo tỷ giá thực tế)
define('VND_TO_USD_RATE', 24000);
?>

