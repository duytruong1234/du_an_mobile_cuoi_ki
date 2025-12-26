<?php
/**
 * test_vnpay_simple.php
 * File test đơn giản để debug VNPay - không cần database
 */

header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Test load config
$config_file = __DIR__ . '/vnpay_config.php';
if (!file_exists($config_file)) {
    echo json_encode([
        'success' => false,
        'error' => 'File vnpay_config.php không tồn tại tại: ' . $config_file
    ]);
    exit;
}

require_once $config_file;

// Kiểm tra constants
$errors = [];
if (!defined('VNPAY_TMN_CODE')) $errors[] = 'VNPAY_TMN_CODE chưa được định nghĩa';
if (!defined('VNPAY_HASH_SECRET')) $errors[] = 'VNPAY_HASH_SECRET chưa được định nghĩa';
if (!defined('VNPAY_URL')) $errors[] = 'VNPAY_URL chưa được định nghĩa';
if (!defined('VNPAY_RETURN_URL')) $errors[] = 'VNPAY_RETURN_URL chưa được định nghĩa';

if (!empty($errors)) {
    echo json_encode([
        'success' => false,
        'errors' => $errors
    ]);
    exit;
}

// Tạo link VNPay test
$madonhang = 'TEST' . date('YmdHis') . rand(100,999);
$vnp_Amount = 23000 * 100; // 23,000 VND (giống trong ảnh)

$inputData = [
    'vnp_Version' => '2.1.0',
    'vnp_TmnCode' => VNPAY_TMN_CODE,
    'vnp_Amount' => (string)$vnp_Amount,
    'vnp_Command' => 'pay',
    'vnp_CreateDate' => date('YmdHis'),
    'vnp_CurrCode' => 'VND',
    'vnp_IpAddr' => '127.0.0.1',
    'vnp_Locale' => 'vn',
    'vnp_OrderInfo' => 'Thanh toan don hang test',
    'vnp_OrderType' => 'billpayment',
    'vnp_ReturnUrl' => VNPAY_RETURN_URL,
    'vnp_TxnRef' => $madonhang,
];

ksort($inputData);
$query = '';
$hashdata = '';
$i = 0;
foreach ($inputData as $key => $value) {
    if ($i === 1) {
        $hashdata .= '&' . urlencode($key) . '=' . urlencode($value);
    } else {
        $hashdata .= urlencode($key) . '=' . urlencode($value);
        $i = 1;
    }
    $query .= urlencode($key) . '=' . urlencode($value) . '&';
}

$vnp_Url = VNPAY_URL . '?' . $query;
$vnpSecureHash = hash_hmac('sha512', $hashdata, VNPAY_HASH_SECRET);
$vnp_Url .= 'vnp_SecureHash=' . $vnpSecureHash;

// Trả về response giống như vnpay_create_payment.php
echo json_encode([
    'success' => true,
    'message' => 'Tạo link thanh toán thành công (TEST MODE)',
    'payment_url' => $vnp_Url,
    'madonhang' => $madonhang,
    'iddonhang' => 999,
    'debug_info' => [
        'url_length' => strlen($vnp_Url),
        'url_starts_with_https' => (strpos($vnp_Url, 'https://') === 0),
        'vnpay_url_base' => VNPAY_URL,
        'tmn_code' => VNPAY_TMN_CODE,
        'hash_secret_length' => strlen(VNPAY_HASH_SECRET)
    ]
], JSON_PRETTY_PRINT);
?>

