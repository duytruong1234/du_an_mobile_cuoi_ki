<?php
header('Content-Type: application/json; charset=utf-8');
include "connect.php";

// Kiểm tra method POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    exit;
}

// Lấy dữ liệu từ POST
$userid = isset($_POST['userid']) ? intval($_POST['userid']) : 0;

// Validate dữ liệu
if ($userid <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'ID người dùng không hợp lệ'
    ]);
    exit;
}

// Kiểm tra user có tồn tại không
$check_query = "SELECT id, username FROM user WHERE id = $userid";
$check_result = mysqli_query($conn, $check_query);

if (!$check_result || mysqli_num_rows($check_result) === 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Người dùng không tồn tại'
    ]);
    exit;
}

$user_data = mysqli_fetch_assoc($check_result);

// Xóa người dùng
// Lưu ý: trong production nên xem xét cascade delete hoặc soft delete
// để không mất dữ liệu đơn hàng liên quan
$delete_query = "DELETE FROM user WHERE id = $userid";
$delete_result = mysqli_query($conn, $delete_query);

if ($delete_result) {
    echo json_encode([
        'success' => true,
        'message' => "Đã xóa người dùng " . $user_data['username'],
        'result' => [
            'userid' => $userid
        ]
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi xóa người dùng: ' . mysqli_error($conn)
    ]);
}

mysqli_close($conn);
?>

