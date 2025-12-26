<?php
/**
 * vnpay_continue_payment.php
 * API tạo lại link thanh toán VNPay cho đơn hàng đã tồn tại
 * Sử dụng khi người dùng muốn tiếp tục thanh toán cho đơn hàng đang chờ thanh toán
 *
 * Request: POST
 * - madonhang: Mã đơn hàng cần thanh toán
 * - tongtien: Tổng tiền (optional, sẽ lấy từ DB nếu không có)
 *
 * Response: JSON
 * - success (bool), message (string), payment_url (string)
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';
require_once __DIR__ . '/vnpay_config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Phương thức không hợp lệ']);
    exit;
}

// --- Nhận dữ liệu ---
$madonhang = isset($_POST['madonhang']) ? $conn->real_escape_string(trim($_POST['madonhang'])) : '';
$tongtien = isset($_POST['tongtien']) ? floatval($_POST['tongtien']) : 0;

if (empty($madonhang)) {
    echo json_encode(['success' => false, 'message' => 'Thiếu mã đơn hàng']);
    exit;
}

// Kiểm tra đơn hàng có tồn tại không
$query = "SELECT id, tongtien, trangthai FROM donhang WHERE madonhang = '{$madonhang}'";
$result = mysqli_query($conn, $query);

if (!$result || mysqli_num_rows($result) === 0) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy đơn hàng']);
    exit;
}

$order = mysqli_fetch_assoc($result);
$iddonhang = $order['id'];
$dbTongtien = floatval($order['tongtien']);
$trangthai = $order['trangthai'];

// Nếu không truyền tổng tiền, lấy từ DB
if ($tongtien <= 0) {
    $tongtien = $dbTongtien;
}

// Kiểm tra tổng tiền hợp lệ
if ($tongtien <= 0) {
    echo json_encode(['success' => false, 'message' => 'Tổng tiền không hợp lệ']);
    exit;
}

// Kiểm tra trạng thái đơn hàng có phù hợp để thanh toán không
// Chỉ cho phép thanh toán với đơn hàng đang chờ thanh toán
$allowedStatuses = ['0', 'Chờ thanh toán', 'Chờ thanh toán VNPay', 'pending'];
$canPay = false;
foreach ($allowedStatuses as $status) {
    if (stripos($trangthai, $status) !== false || $trangthai === $status) {
        $canPay = true;
        break;
    }
}

if (!$canPay) {
    echo json_encode([
        'success' => false,
        'message' => 'Đơn hàng không ở trạng thái chờ thanh toán. Trạng thái hiện tại: ' . $trangthai
    ]);
    exit;
}

// --- Tạo URL thanh toán VNPay ---
// Tạo mã giao dịch duy nhất bằng cách thêm timestamp và random number
// VNPay không cho phép tạo giao dịch mới với cùng mã đã tồn tại
$vnp_TxnRef = $madonhang . '_' . date('YmdHis') . rand(100, 999);
$vnp_OrderInfo = 'Thanh toan don hang ' . $madonhang;
$vnp_OrderType = 'billpayment';
$vnp_Amount = $tongtien * 100; // nhân 100 theo quy ước VNPay
$vnp_Locale = 'vn';
$vnp_IpAddr = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';

$inputData = [
    'vnp_Version' => '2.1.0',
    'vnp_TmnCode' => VNPAY_TMN_CODE,
    'vnp_Amount' => (string)$vnp_Amount,
    'vnp_Command' => 'pay',
    'vnp_CreateDate' => date('YmdHis'),
    'vnp_CurrCode' => 'VND',
    'vnp_IpAddr' => $vnp_IpAddr,
    'vnp_Locale' => $vnp_Locale,
    'vnp_OrderInfo' => $vnp_OrderInfo,
    'vnp_OrderType' => $vnp_OrderType,
    'vnp_ReturnUrl' => VNPAY_RETURN_URL,
    'vnp_TxnRef' => $vnp_TxnRef,
];

// Sắp xếp theo key
ksort($inputData);

// Tạo query string
$query = '';
$i = 0;
$hashdata = '';
foreach ($inputData as $key => $value) {
    if ($i == 1) {
        $hashdata .= '&' . urlencode($key) . "=" . urlencode($value);
    } else {
        $hashdata .= urlencode($key) . "=" . urlencode($value);
        $i = 1;
    }
    $query .= urlencode($key) . "=" . urlencode($value) . '&';
}

// Tạo secure hash
$vnp_Url = VNPAY_URL . "?" . $query;
$vnpSecureHash = hash_hmac('sha512', $hashdata, VNPAY_HASH_SECRET);
$vnp_Url .= 'vnp_SecureHash=' . $vnpSecureHash;

// Cập nhật lại trạng thái đơn hàng (đảm bảo là "Chờ thanh toán VNPay")
$updateQuery = "UPDATE donhang SET trangthai = 'Chờ thanh toán VNPay' WHERE madonhang = '{$madonhang}'";
mysqli_query($conn, $updateQuery);

// Trả về kết quả
echo json_encode([
    'success' => true,
    'message' => 'Tạo link thanh toán thành công',
    'payment_url' => $vnp_Url,
    'madonhang' => $madonhang,
    'tongtien' => $tongtien
]);

mysqli_close($conn);
?>

