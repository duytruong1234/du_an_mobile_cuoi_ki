<?php
/**
 * vnpay_return.php
 * API xác thực kết quả thanh toán VNPay, cập nhật trạng thái đơn hàng, giảm tồn kho, trả về JSON
 * Input: GET các tham số vnp_*
 * Output: JSON { success, message, status, madonhang, vnp_TransactionNo, vnp_BankCode, vnp_PayDate }
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';
require_once __DIR__ . '/vnpay_config.php';

// Thu thập các tham số bắt đầu bằng vnp_
$inputData = array();
foreach ($_GET as $key => $value) {
    if (substr($key, 0, 4) === 'vnp_') {
        $inputData[$key] = $value;
    }
}

// ✅ LOGGING DEBUG - Ghi toàn bộ dữ liệu VNPay trả về
file_put_contents(__DIR__ . '/vnpay_debug_return.txt', json_encode([
    'timestamp' => date('Y-m-d H:i:s'),
    'all_get_params' => $_GET
], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . "\n" . str_repeat('=', 80) . "\n\n", FILE_APPEND);

// Lấy secure hash gửi về
$vnp_SecureHash = isset($inputData['vnp_SecureHash']) ? $inputData['vnp_SecureHash'] : '';
unset($inputData['vnp_SecureHash'], $inputData['vnp_SecureHashType']);

ksort($inputData);
$hashData = '';
$i = 0;
foreach ($inputData as $key => $value) {
    if ($i === 0) {
        $hashData .= urlencode($key) . '=' . urlencode($value);
    } else {
        $hashData .= '&' . urlencode($key) . '=' . urlencode($value);
    }
    $i++;
}

$secureHash = hash_hmac('sha512', $hashData, VNPAY_HASH_SECRET);

// ✅ LOGGING DEBUG - So sánh hash
file_put_contents(__DIR__ . '/vnpay_debug_return.txt', json_encode([
    'hashData_raw' => $hashData,
    'vnp_SecureHash_from_vnpay' => $vnp_SecureHash,
    'secureHash_calculated' => $secureHash,
    'hash_match' => ($secureHash === $vnp_SecureHash)
], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . "\n" . str_repeat('-', 80) . "\n\n", FILE_APPEND);

$vnp_TxnRef = $inputData['vnp_TxnRef'] ?? '';
$vnp_ResponseCode = $inputData['vnp_ResponseCode'] ?? '';
$vnp_TransactionNo = $inputData['vnp_TransactionNo'] ?? '';
$vnp_BankCode = $inputData['vnp_BankCode'] ?? '';
$vnp_PayDate = $inputData['vnp_PayDate'] ?? '';
$vnp_Amount = $inputData['vnp_Amount'] ?? 0;

if ($secureHash !== $vnp_SecureHash) {
    echo json_encode([
        'success' => false,
        'message' => 'Chữ ký không hợp lệ',
        'status' => 'invalid_hash'
    ]);
    mysqli_close($conn);
    exit;
}

// Tìm đơn hàng
$madonhang_goc = $vnp_TxnRef;
if (strpos($vnp_TxnRef, '_') !== false) {
    $madonhang_goc = explode('_', $vnp_TxnRef)[0];
}

$txnRefEsc = $conn->real_escape_string($madonhang_goc);
$q = mysqli_query($conn, "SELECT * FROM donhang WHERE madonhang = '$txnRefEsc' LIMIT 1");
if (!$q || mysqli_num_rows($q) === 0) {
    $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($madonhang_goc) . "&status=error&message=" . urlencode("Không tìm thấy đơn hàng");
    redirectToApp($deepLink, 'error', $madonhang_goc, 0, '', '', 'Không tìm thấy đơn hàng');
    exit;
}

$order = mysqli_fetch_assoc($q);
$iddonhang = intval($order['id']);

if ($vnp_ResponseCode === '00') {
    // Thanh toán thành công
    mysqli_begin_transaction($conn);
    try {
        $txnNoEsc = $conn->real_escape_string($vnp_TransactionNo);
        $bankCodeEsc = $conn->real_escape_string($vnp_BankCode);
        $payDateEsc = $conn->real_escape_string($vnp_PayDate);

        $sqlUpdate = "UPDATE donhang 
                      SET trangthai = 'Đã thanh toán', 
                          vnpay_transaction_no = '$txnNoEsc', 
                          vnpay_bank_code = '$bankCodeEsc', 
                          vnpay_pay_date = '$payDateEsc' 
                      WHERE id = $iddonhang";
        mysqli_query($conn, $sqlUpdate);

      

        mysqli_commit($conn);

        // Redirect thành công
        $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=success&amount=" . urlencode($vnp_Amount);
        redirectToApp($deepLink, 'success', $vnp_TxnRef, $vnp_Amount, $vnp_TransactionNo, $vnp_BankCode);
        exit;

    } catch (Exception $e) {
        mysqli_rollback($conn);
        $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=error&message=" . urlencode($e->getMessage());
        redirectToApp($deepLink, 'error', $vnp_TxnRef, 0, '', '', $e->getMessage());
        exit;
    }
} else {
    // Thanh toán thất bại hoặc bị hủy
    mysqli_query($conn, "UPDATE donhang SET trangthai = 'Đã hủy' WHERE id = $iddonhang");
    $deepLink = "appbandienthoai://payment_return?madonhang=" . urlencode($vnp_TxnRef) . "&status=failed&code=" . urlencode($vnp_ResponseCode);
    redirectToApp($deepLink, 'failed', $vnp_TxnRef, 0, '', '', $vnp_ResponseCode);
    exit;
}

mysqli_close($conn);

// === HÀM REDIRECT VỀ APP ===
function redirectToApp($deepLink, $status, $madonhang, $amount = 0, $transactionNo = '', $bankCode = '', $error = '') {
    header("Location: " . $deepLink);
    exit;
}
?>
