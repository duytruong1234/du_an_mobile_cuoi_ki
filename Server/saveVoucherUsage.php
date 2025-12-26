<?php
/**
 * API LƯU LỊCH SỬ SỬ DỤNG VOUCHER
 * Endpoint: saveVoucherUsage.php
 * Method: POST
 *
 * Gọi sau khi tạo đơn hàng thành công
 *
 * Params:
 * - voucher_id: ID voucher
 * - user_id: ID người dùng
 * - donhang_id: ID đơn hàng (sau khi insert)
 * - ma_donhang: Mã đơn hàng
 * - gia_tri_don_hang: Giá trị đơn hàng trước giảm
 * - gia_tri_giam: Số tiền đã giảm
 */

include "connect.php";
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $voucher_id = isset($_POST['voucher_id']) ? intval($_POST['voucher_id']) : 0;
    $user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
    $donhang_id = isset($_POST['donhang_id']) ? intval($_POST['donhang_id']) : null;
    $ma_donhang = isset($_POST['ma_donhang']) ? trim($_POST['ma_donhang']) : null;
    $gia_tri_don_hang = isset($_POST['gia_tri_don_hang']) ? floatval($_POST['gia_tri_don_hang']) : 0;
    $gia_tri_giam = isset($_POST['gia_tri_giam']) ? floatval($_POST['gia_tri_giam']) : 0;

    // Validate
    if ($voucher_id <= 0 || $user_id <= 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Dữ liệu không hợp lệ'
        ]);
        exit;
    }

    // Insert vào bảng voucher_usage
    $query = "INSERT INTO voucher_usage
              (voucher_id, user_id, donhang_id, ma_donhang, gia_tri_don_hang, gia_tri_giam)
              VALUES (?, ?, ?, ?, ?, ?)";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("iiisdd", $voucher_id, $user_id, $donhang_id, $ma_donhang, $gia_tri_don_hang, $gia_tri_giam);

    if ($stmt->execute()) {
        echo json_encode([
            'success' => true,
            'message' => 'Lưu lịch sử sử dụng voucher thành công',
            'usage_id' => $stmt->insert_id
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi: ' . $stmt->error
        ]);
    }

    $stmt->close();
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
}

$conn->close();
?>

