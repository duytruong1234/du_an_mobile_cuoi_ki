<?php
/**
 * API KIỂM TRA VÀ ÁP DỤNG VOUCHER
 * Endpoint: checkVoucher.php
 * Method: POST
 *
 * Params:
 * - ma_voucher: Mã voucher cần kiểm tra
 * - user_id: ID người dùng
 * - tong_tien: Tổng tiền đơn hàng
 */

include "connect.php";
header('Content-Type: application/json; charset=utf-8');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $ma_voucher = isset($_POST['ma_voucher']) ? trim($_POST['ma_voucher']) : '';
    $user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
    $tong_tien = isset($_POST['tong_tien']) ? floatval($_POST['tong_tien']) : 0;

    // Validate input
    if (empty($ma_voucher) || $user_id <= 0 || $tong_tien <= 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Dữ liệu không hợp lệ'
        ]);
        exit;
    }

    // 1. Kiểm tra voucher có tồn tại và đang hoạt động
    $query = "SELECT * FROM voucher
              WHERE ma_voucher = ?
              AND trang_thai = 1
              AND ngay_bat_dau <= NOW()
              AND ngay_het_han >= NOW()";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("s", $ma_voucher);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows == 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Mã voucher không tồn tại hoặc đã hết hạn'
        ]);
        exit;
    }

    $voucher = $result->fetch_assoc();

    // 2. Kiểm tra số lượng voucher còn lại
    if ($voucher['so_luong'] !== null && $voucher['da_su_dung'] >= $voucher['so_luong']) {
        echo json_encode([
            'success' => false,
            'message' => 'Mã voucher đã hết lượt sử dụng'
        ]);
        exit;
    }

    // 3. Kiểm tra giá trị đơn hàng tối thiểu
    if ($tong_tien < $voucher['don_toi_thieu']) {
        echo json_encode([
            'success' => false,
            'message' => 'Đơn hàng tối thiểu ' . number_format($voucher['don_toi_thieu'], 0, ',', '.') . 'đ để áp dụng mã này',
            'don_toi_thieu' => $voucher['don_toi_thieu']
        ]);
        exit;
    }

    // 4. Kiểm tra điều kiện áp dụng cho user
    switch ($voucher['ap_dung_cho']) {
        case 'new_user':
            // Kiểm tra user mới (chưa có đơn hàng nào)
            $check_query = "SELECT COUNT(*) as count FROM donhang WHERE iduser = ?";
            $check_stmt = $conn->prepare($check_query);
            $check_stmt->bind_param("i", $user_id);
            $check_stmt->execute();
            $check_result = $check_stmt->get_result();
            $count = $check_result->fetch_assoc()['count'];

            if ($count > 0) {
                echo json_encode([
                    'success' => false,
                    'message' => 'Mã voucher chỉ dành cho khách hàng mới'
                ]);
                exit;
            }
            break;

        case 'first_order':
            // Kiểm tra đơn hàng đầu tiên
            $check_query = "SELECT COUNT(*) as count FROM donhang WHERE iduser = ?";
            $check_stmt = $conn->prepare($check_query);
            $check_stmt->bind_param("i", $user_id);
            $check_stmt->execute();
            $check_result = $check_stmt->get_result();
            $count = $check_result->fetch_assoc()['count'];

            if ($count > 0) {
                echo json_encode([
                    'success' => false,
                    'message' => 'Mã voucher chỉ áp dụng cho đơn hàng đầu tiên'
                ]);
                exit;
            }
            break;

        case 'old_user':
            // Kiểm tra khách hàng cũ (đã có ít nhất 1 đơn hàng)
            $check_query = "SELECT COUNT(*) as count FROM donhang WHERE iduser = ?";
            $check_stmt = $conn->prepare($check_query);
            $check_stmt->bind_param("i", $user_id);
            $check_stmt->execute();
            $check_result = $check_stmt->get_result();
            $count = $check_result->fetch_assoc()['count'];

            if ($count == 0) {
                echo json_encode([
                    'success' => false,
                    'message' => 'Mã voucher chỉ dành cho khách hàng thân thiết'
                ]);
                exit;
            }
            break;
    }

    // 5. Kiểm tra số lần user đã dùng voucher này
    $usage_query = "SELECT COUNT(*) as count FROM voucher_usage
                    WHERE voucher_id = ? AND user_id = ?";
    $usage_stmt = $conn->prepare($usage_query);
    $usage_stmt->bind_param("ii", $voucher['id'], $user_id);
    $usage_stmt->execute();
    $usage_result = $usage_stmt->get_result();
    $usage_count = $usage_result->fetch_assoc()['count'];

    if ($usage_count >= $voucher['gioi_han_moi_user']) {
        echo json_encode([
            'success' => false,
            'message' => 'Bạn đã sử dụng hết lượt cho mã này'
        ]);
        exit;
    }

    // 6. Tính số tiền được giảm
    $gia_tri_giam = 0;

    switch ($voucher['loai_giam']) {
        case 'percent':
            // Giảm theo %
            $gia_tri_giam = ($tong_tien * $voucher['gia_tri_giam']) / 100;

            // Kiểm tra giảm tối đa
            if ($voucher['giam_toi_da'] !== null && $gia_tri_giam > $voucher['giam_toi_da']) {
                $gia_tri_giam = $voucher['giam_toi_da'];
            }
            break;

        case 'fixed':
            // Giảm số tiền cố định
            $gia_tri_giam = $voucher['gia_tri_giam'];
            break;

        case 'freeship':
            // Miễn phí ship
            $gia_tri_giam = $voucher['gia_tri_giam'];
            break;
    }

    // Đảm bảo giá trị giảm không lớn hơn tổng tiền
    if ($gia_tri_giam > $tong_tien) {
        $gia_tri_giam = $tong_tien;
    }

    // Tổng tiền sau khi giảm
    $tong_sau_giam = $tong_tien - $gia_tri_giam;

    // 7. Trả về kết quả thành công
    echo json_encode([
        'success' => true,
        'message' => 'Áp dụng mã giảm giá thành công',
        'voucher' => [
            'id' => $voucher['id'],
            'ma_voucher' => $voucher['ma_voucher'],
            'ten_voucher' => $voucher['ten_voucher'],
            'loai_giam' => $voucher['loai_giam'],
            'gia_tri_giam_voucher' => $voucher['gia_tri_giam'],
            'mo_ta' => $voucher['mo_ta']
        ],
        'tinh_toan' => [
            'tong_truoc_giam' => $tong_tien,
            'gia_tri_giam' => $gia_tri_giam,
            'tong_sau_giam' => $tong_sau_giam
        ]
    ]);

} else {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
}

$conn->close();
?>

