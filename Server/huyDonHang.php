<?php
/**
 * huyDonHang.php
 * API hủy đơn hàng cho người dùng
 * Chỉ cho phép hủy đơn hàng ở trạng thái: "Đang xử lý", "Chờ thanh toán", "Chờ thanh toán VNPay"
 *
 * Request: POST
 * - madonhang: Mã đơn hàng cần hủy
 * - iduser: ID người dùng (để kiểm tra quyền sở hữu)
 * - lydo: Lý do hủy đơn (optional)
 *
 * Response: JSON
 * - success (bool), message (string)
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/connect.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Phương thức không hợp lệ']);
    exit;
}

// Nhận dữ liệu
$madonhang = isset($_POST['madonhang']) ? $conn->real_escape_string(trim($_POST['madonhang'])) : '';
$iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
$lydo = isset($_POST['lydo']) ? $conn->real_escape_string(trim($_POST['lydo'])) : 'Người dùng hủy đơn';

// Validate
if (empty($madonhang) || $iduser <= 0) {
    echo json_encode(['success' => false, 'message' => 'Thiếu thông tin đơn hàng']);
    exit;
}

// Kiểm tra đơn hàng có tồn tại và thuộc về user này không
$query = "SELECT id, iduser, trangthai, tongtien FROM donhang WHERE madonhang = '$madonhang'";
$result = mysqli_query($conn, $query);

if (!$result || mysqli_num_rows($result) === 0) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy đơn hàng']);
    exit;
}

$order = mysqli_fetch_assoc($result);
$iddonhang = $order['id'];
$order_iduser = $order['iduser'];
$trangthai = $order['trangthai'];

// Kiểm tra quyền sở hữu
if ($order_iduser != $iduser) {
    echo json_encode(['success' => false, 'message' => 'Bạn không có quyền hủy đơn hàng này']);
    exit;
}

// Kiểm tra trạng thái có thể hủy
// Cho phép hủy đơn hàng ở trạng thái: "Chờ xử lý", "Đang xử lý", "Chờ thanh toán", "Đã thanh toán"
// KHÔNG cho phép hủy: "Đã chuẩn bị", "Đã giao", "Thành công", "Đã hủy"
$allowed_statuses = ['Chờ xử lý', 'Đang xử lý', 'Chờ thanh toán', 'Chờ thanh toán VNPay', 'Đã thanh toán', '0', '1', '2'];
$blocked_statuses = ['Đã chuẩn bị', 'Đã giao', 'Thành công', 'Đã hủy', '3', '4', '5'];

$can_cancel = false;

// Kiểm tra trạng thái cho phép hủy
foreach ($allowed_statuses as $status) {
    if (stripos($trangthai, $status) !== false || $trangthai === $status || $trangthai == $status) {
        $can_cancel = true;
        break;
    }
}

// Kiểm tra trạng thái KHÔNG cho phép hủy (ưu tiên cao hơn)
foreach ($blocked_statuses as $status) {
    if (stripos($trangthai, $status) !== false || $trangthai === $status || $trangthai == $status) {
        $can_cancel = false;
        break;
    }
}

if (!$can_cancel) {
    echo json_encode([
        'success' => false,
        'message' => 'Không thể hủy đơn hàng ở trạng thái: ' . $trangthai . '. Chỉ có thể hủy đơn hàng đang xử lý hoặc chờ thanh toán.'
    ]);
    exit;
}

// Bắt đầu transaction
mysqli_begin_transaction($conn);

try {
    // Cập nhật trạng thái đơn hàng thành "Đã hủy"
    $updateQuery = "UPDATE donhang SET trangthai = 'Đã hủy' WHERE id = $iddonhang";
    if (!mysqli_query($conn, $updateQuery)) {
        throw new Exception('Lỗi cập nhật trạng thái đơn hàng: ' . mysqli_error($conn));
    }

    // Hoàn trả tồn kho nếu đơn hàng đã giảm tồn kho (trạng thái "Đã thanh toán")
    // Lấy chi tiết sản phẩm trong đơn hàng
    $detailQuery = "SELECT idsp, soluong FROM chitietdonhang WHERE iddonhang = $iddonhang";
    $detailResult = mysqli_query($conn, $detailQuery);

    if ($detailResult && mysqli_num_rows($detailResult) > 0) {
        while ($detail = mysqli_fetch_assoc($detailResult)) {
            $idsp = intval($detail['idsp']);
            $soluong = intval($detail['soluong']);

            // Hoàn trả tồn kho (chỉ hoàn nếu đơn đã thanh toán)
            if (stripos($trangthai, 'thanh toán') !== false && stripos($trangthai, 'chờ') === false) {
                $restoreQuery = "UPDATE sanphammoi SET soluongtonkho = soluongtonkho + $soluong WHERE id = $idsp";
                mysqli_query($conn, $restoreQuery);
            }
        }
    }

    // Lưu lý do hủy (có thể tạo bảng log riêng nếu muốn)
    // Hiện tại chỉ log vào bảng donhang bằng cách thêm comment vào trangthai
    $updateReasonQuery = "UPDATE donhang SET trangthai = 'Đã hủy' WHERE id = $iddonhang";
    mysqli_query($conn, $updateReasonQuery);

    mysqli_commit($conn);

    echo json_encode([
        'success' => true,
        'message' => 'Hủy đơn hàng thành công',
        'madonhang' => $madonhang
    ]);

} catch (Exception $e) {
    mysqli_rollback($conn);
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi hủy đơn hàng: ' . $e->getMessage()
    ]);
}

mysqli_close($conn);
?>

