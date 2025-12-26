<?php
// ⚙️ Cấu hình tài khoản VNPay Sandbox
if (!defined('VNPAY_TMN_CODE')) define('VNPAY_TMN_CODE', '1C93XWXU');
if (!defined('VNPAY_HASH_SECRET')) define('VNPAY_HASH_SECRET', 'GE4QRFHMNXHQR9XDR7VIY8HSCCSAHYDM');
if (!defined('VNPAY_URL')) define('VNPAY_URL', 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html');

// ⚠️ Dùng đúng domain public (Ngrok)
if (!defined('VNPAY_RETURN_URL')) define('VNPAY_RETURN_URL', 'https://donya-barwise-subversively.ngrok-free.dev/appbandienthoai/vnpay_return.php');


date_default_timezone_set('Asia/Ho_Chi_Minh');
?>
