<?php
/**
 * vnpay_check_status.php
 * API kiểm tra trạng thái thanh toán của đơn hàng
 * Input: GET hoặc POST - madonhang
 * Output: JSON { success, message, data }
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';

// Lấy madonhang từ GET hoặc POST
$madonhang = '';
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $madonhang = isset($_GET['madonhang']) ? trim($_GET['madonhang']) : '';
} else {
    $madonhang = isset($_POST['madonhang']) ? trim($_POST['madonhang']) : '';
}

if (empty($madonhang)) {
    echo json_encode(['success' => false, 'message' => 'Thiếu tham số madonhang']);
    exit;
}

$madonhang_esc = $conn->real_escape_string($madonhang);
$sql = "SELECT id, madonhang, iduser, diachi, sodienthoai, soluong, tongtien, trangthai, vnpay_transaction_no, vnpay_bank_code, vnpay_pay_date, ngaydat, ngaygiaodukien
        FROM donhang WHERE madonhang = '$madonhang_esc' LIMIT 1";

$res = mysqli_query($conn, $sql);
if (!$res) {
    echo json_encode(['success' => false, 'message' => 'Lỗi truy vấn: ' . mysqli_error($conn)]);
    exit;
}

if (mysqli_num_rows($res) === 0) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy đơn hàng']);
    exit;
}

$order = mysqli_fetch_assoc($res);

echo json_encode([
    'success' => true,
    'message' => 'Lấy thông tin đơn hàng thành công',
    'data' => $order
]);

mysqli_close($conn);
?>
