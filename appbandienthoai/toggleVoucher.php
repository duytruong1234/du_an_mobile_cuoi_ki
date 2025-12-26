<?php
/**
 * API BẬT/TẮT VOUCHER
 * Endpoint: admin/toggleVoucher.php
 * Method: POST
 * Params:
 * - id: ID voucher
 */

include "connect.php";
header('Content-Type: application/json; charset=utf-8');

ini_set('display_errors', 0);
error_reporting(0);

session_start();
if (isset($_SESSION['user']) && $_SESSION['user']['role'] != 1) {
    echo json_encode([
        'success' => false,
        'message' => 'Không có quyền truy cập'
    ]);
    return;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    return;
}

if (!isset($_POST['id']) || intval($_POST['id']) <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'ID voucher không hợp lệ'
    ]);
    return;
}

$id = intval($_POST['id']);

try {
    // --- Kiểm tra voucher tồn tại ---
    $check_query = "SELECT trang_thai FROM voucher WHERE id = ?";
    $check_stmt = $conn->prepare($check_query);
    $check_stmt->bind_param("i", $id);
    $check_stmt->execute();
    $check_result = $check_stmt->get_result();

    if ($check_result->num_rows == 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Voucher không tồn tại'
        ]);
        return;
    }

    $voucher = $check_result->fetch_assoc();
    $new_status = $voucher['trang_thai'] == 1 ? 0 : 1;

    // --- Cập nhật trạng thái ---
    $update_query = "UPDATE voucher SET trang_thai = ?, updated_at = NOW() WHERE id = ?";
    $update_stmt = $conn->prepare($update_query);
    $update_stmt->bind_param("ii", $new_status, $id);

    if ($update_stmt->execute()) {
        echo json_encode([
            'success' => true,
            'message' => $new_status == 1 ? 'Đã bật voucher' : 'Đã tắt voucher',
            'new_status' => $new_status
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi khi cập nhật: ' . $update_stmt->error
        ]);
    }

    $update_stmt->close();
    $check_stmt->close();

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi server: ' . $e->getMessage()
    ]);
}

$conn->close();
