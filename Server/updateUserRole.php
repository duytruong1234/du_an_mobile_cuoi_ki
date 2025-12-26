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
$role = isset($_POST['role']) ? intval($_POST['role']) : 0;

// Validate dữ liệu
if ($userid <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'ID người dùng không hợp lệ'
    ]);
    exit;
}

// Kiểm tra role chỉ có thể là 0 hoặc 1
if ($role !== 0 && $role !== 1) {
    echo json_encode([
        'success' => false,
        'message' => 'Quyền không hợp lệ (chỉ 0 hoặc 1)'
    ]);
    exit;
}

// Kiểm tra user có tồn tại không
$check_query = "SELECT id FROM user WHERE id = $userid";
$check_result = mysqli_query($conn, $check_query);

if (!$check_result || mysqli_num_rows($check_result) === 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Người dùng không tồn tại'
    ]);
    exit;
}

// Cập nhật role
$update_query = "UPDATE user SET role = $role WHERE id = $userid";
$update_result = mysqli_query($conn, $update_query);

if ($update_result) {
    $roleText = $role === 1 ? 'Admin' : 'User';
    echo json_encode([
        'success' => true,
        'message' => "Đã cập nhật quyền thành $roleText",
        'result' => [
            'userid' => $userid,
            'role' => $role
        ]
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi cập nhật: ' . mysqli_error($conn)
    ]);
}

mysqli_close($conn);
?>

