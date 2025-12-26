<?php
/**
 * test_vnpay_config.php
 * File test để kiểm tra cấu hình VNPay và tạo link thanh toán mẫu
 */

header('Content-Type: application/json; charset=utf-8');

// Test 1: Kiểm tra file config có load được không
if (!file_exists(__DIR__ . '/vnpay_config.php')) {
    echo json_encode(['error' => 'File vnpay_config.php không tồn tại']);
    exit;
}

require_once __DIR__ . '/vnpay_config.php';

// Test 2: Kiểm tra constants có được định nghĩa không
if (!defined('VNPAY_TMN_CODE')) {
    echo json_encode(['error' => 'VNPAY_TMN_CODE chưa được định nghĩa']);
    exit;
}

// Test 3: Tạo link VNPay mẫu
$vnp_TxnRef = 'TEST' . date('YmdHis');
$vnp_Amount = 100000 * 100; // 100,000 VND
$vnp_OrderInfo = 'Test thanh toan';

$inputData = [
    'vnp_Version' => '2.1.0',
    'vnp_TmnCode' => VNPAY_TMN_CODE,
    'vnp_Amount' => (string)$vnp_Amount,
    'vnp_Command' => 'pay',
    'vnp_CreateDate' => date('YmdHis'),
    'vnp_CurrCode' => 'VND',
    'vnp_IpAddr' => '127.0.0.1',
    'vnp_Locale' => 'vn',
    'vnp_OrderInfo' => $vnp_OrderInfo,
    'vnp_OrderType' => 'billpayment',
    'vnp_ReturnUrl' => VNPAY_RETURN_URL,
    'vnp_TxnRef' => $vnp_TxnRef,
];

ksort($inputData);
$query = '';
$hashdata = '';
$i = 0;
foreach ($inputData as $key => $value) {
    $encodedKey = urlencode($key);
    $encodedValue = urlencode($value);
    if ($i === 1) {
        $hashdata .= '&' . $encodedKey . '=' . $encodedValue;
    } else {
        $hashdata .= $encodedKey . '=' . $encodedValue;
        $i = 1;
    }
    $query .= $encodedKey . '=' . $encodedValue . '&';
}

$vnp_Url = VNPAY_URL . '?' . $query;
$vnpSecureHash = hash_hmac('sha512', $hashdata, VNPAY_HASH_SECRET);
$vnp_Url .= 'vnp_SecureHash=' . $vnpSecureHash;

// Trả về kết quả
echo json_encode([
    'success' => true,
    'config' => [
        'VNPAY_TMN_CODE' => VNPAY_TMN_CODE,
        'VNPAY_URL' => VNPAY_URL,
        'VNPAY_RETURN_URL' => VNPAY_RETURN_URL,
        'HASH_SECRET_LENGTH' => strlen(VNPAY_HASH_SECRET)
    ],
    'test_payment_url' => $vnp_Url,
    'url_length' => strlen($vnp_Url),
    'url_starts_with_https' => (strpos($vnp_Url, 'https://') === 0)
], JSON_PRETTY_PRINT);
?>

