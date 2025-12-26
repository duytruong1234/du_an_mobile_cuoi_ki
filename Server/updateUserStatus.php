<?php
/**
 * updateUserStatus.php
 * API cập nhật trạng thái tài khoản (Khóa/Mở khóa)
 */

header('Content-Type: application/json; charset=utf-8');
include "connect.php";

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

$userId = isset($_POST['userId']) ? intval($_POST['userId']) : 0;
$status = isset($_POST['status']) ? intval($_POST['status']) : 0;

if ($userId <= 0) {
    echo json_encode(['success' => false, 'message' => 'User ID không hợp lệ']);
    exit;
}

// Validate status (chỉ cho phép 0 hoặc 1)
if ($status !== 0 && $status !== 1) {
    echo json_encode(['success' => false, 'message' => 'Trạng thái không hợp lệ']);
    exit;
}

// Cập nhật trạng thái
$query = "UPDATE user SET status = $status WHERE id = $userId";

if (mysqli_query($conn, $query)) {
    $statusText = $status == 1 ? 'Đã mở khóa' : 'Đã khóa';
    echo json_encode([
        'success' => true,
        'message' => $statusText . ' tài khoản thành công'
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Lỗi: ' . mysqli_error($conn)]);
}

mysqli_close($conn);
?>

