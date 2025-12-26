<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $madonhang = isset($_POST['madonhang']) ? $conn->real_escape_string(trim($_POST['madonhang'])) : '';
    $trangthai = isset($_POST['trangthai']) ? $conn->real_escape_string(trim($_POST['trangthai'])) : '';

    if (empty($madonhang) || empty($trangthai)) {
        echo json_encode([
            'success' => false,
            'message' => 'Thiếu thông tin'
        ]);
        exit;
    }

    // Kiểm tra trạng thái hiện tại của đơn hàng
    $checkQuery = "SELECT trangthai FROM donhang WHERE madonhang = '$madonhang'";
    $result = mysqli_query($conn, $checkQuery);

    if (!$result || mysqli_num_rows($result) === 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Không tìm thấy đơn hàng'
        ]);
        exit;
    }

    $order = mysqli_fetch_assoc($result);
    $currentStatus = $order['trangthai'];

    // Kiểm tra nếu đơn hàng đã bị hủy, không cho phép cập nhật
    if (stripos($currentStatus, 'Đã hủy') !== false || $currentStatus === 'Đã hủy') {
        echo json_encode([
            'success' => false,
            'message' => 'Không thể cập nhật đơn hàng đã bị hủy'
        ]);
        exit;
    }

    // Cập nhật trạng thái
    $sql = "UPDATE donhang
            SET trangthai = '$trangthai'
            WHERE madonhang = '$madonhang'";

    if (mysqli_query($conn, $sql)) {
        echo json_encode([
            'success' => true,
            'message' => 'Cập nhật trạng thái thành công'
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi cập nhật: ' . mysqli_error($conn)
        ]);
    }

    mysqli_close($conn);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không hợp lệ'
    ]);
}
?>

