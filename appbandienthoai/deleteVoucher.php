<?php


error_reporting(0);
ini_set('display_errors', 0);

include "connect.php";
header('Content-Type: application/json; charset=utf-8');

// Kiểm tra method POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    exit;
}

// Kiểm tra role
if (!isset($_POST['role']) || intval($_POST['role']) != 1) {
    echo json_encode([
        'success' => false,
        'message' => 'Không có quyền xóa voucher. Chỉ admin mới có quyền này.'
    ]);
    exit;
}

// Kiểm tra ID voucher
if (!isset($_POST['id']) || intval($_POST['id']) <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'ID voucher không hợp lệ'
    ]);
    exit;
}

$id = intval($_POST['id']);
$hard_delete = isset($_POST['hard_delete']) && $_POST['hard_delete'] === 'true';

// Kiểm tra voucher có tồn tại không
$check_query = "SELECT ma_voucher, da_su_dung FROM voucher WHERE id = ?";
$check_stmt = $conn->prepare($check_query);
$check_stmt->bind_param("i", $id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows == 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Voucher không tồn tại'
    ]);
    exit;
}

$voucher = $check_result->fetch_assoc();

// Nếu voucher đã được sử dụng, không cho phép xóa cứng
if ($hard_delete && $voucher['da_su_dung'] > 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Không thể xóa voucher đã được sử dụng. Chỉ có thể tắt voucher này.'
    ]);
    exit;
}

if ($hard_delete) {
    // Xóa cứng - xóa hoàn toàn khỏi database
    $query = "DELETE FROM voucher WHERE id = ?";
} else {
    // Xóa mềm - chỉ set trạng thái = 0
    $query = "UPDATE voucher SET trang_thai = 0 WHERE id = ?";
}

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => $hard_delete ? 'Xóa voucher thành công' : 'Đã tắt voucher',
        'deleted_type' => $hard_delete ? 'hard' : 'soft'
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi: ' . $stmt->error
    ]);
}

$stmt->close();
$conn->close();
?>

