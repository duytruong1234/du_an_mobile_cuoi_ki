<?php
/**
 * API LẤY DANH SÁCH VOUCHER KHẢ DỤNG
 * Endpoint: getVouchers.php
 * Method: POST/GET
 *
 * Params:
 * - user_id: ID người dùng (để lọc voucher phù hợp)
 * - tong_tien: Tổng tiền đơn hàng hiện tại (optional)
 */

include "connect.php";
header('Content-Type: application/json; charset=utf-8');

$user_id = isset($_REQUEST['user_id']) ? intval($_REQUEST['user_id']) : 0;
$tong_tien = isset($_REQUEST['tong_tien']) ? floatval($_REQUEST['tong_tien']) : 0;

if ($user_id <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'User ID không hợp lệ'
    ]);
    exit;
}

// Kiểm tra xem user đã có đơn hàng chưa (để phân loại new/old user)
$order_query = "SELECT COUNT(*) as count FROM donhang WHERE iduser = ?";
$order_stmt = $conn->prepare($order_query);
$order_stmt->bind_param("i", $user_id);
$order_stmt->execute();
$order_result = $order_stmt->get_result();
$order_count = $order_result->fetch_assoc()['count'];

$is_new_user = ($order_count == 0);

// Lấy danh sách voucher đang hoạt động
$query = "SELECT v.*,
          (SELECT COUNT(*) FROM voucher_usage WHERE voucher_id = v.id AND user_id = ?) as user_used_count
          FROM voucher v
          WHERE v.trang_thai = 1
          AND v.ngay_bat_dau <= NOW()
          AND v.ngay_het_han >= NOW()
          AND (v.so_luong IS NULL OR v.da_su_dung < v.so_luong)
          AND (
              v.ap_dung_cho = 'all'
              OR (v.ap_dung_cho = 'new_user' AND ? = 1)
              OR (v.ap_dung_cho = 'old_user' AND ? = 0)
              OR (v.ap_dung_cho = 'first_order' AND ? = 1)
          )
          ORDER BY v.gia_tri_giam DESC";

$is_new_int = $is_new_user ? 1 : 0;
$stmt = $conn->prepare($query);
$stmt->bind_param("iiii", $user_id, $is_new_int, $is_new_int, $is_new_int);
$stmt->execute();
$result = $stmt->get_result();

$vouchers = [];
$vouchers_applicable = []; // Voucher có thể dùng ngay
$vouchers_not_applicable = []; // Voucher chưa đủ điều kiện

while ($row = $result->fetch_assoc()) {
    // Kiểm tra user đã dùng hết lượt chưa
    if ($row['user_used_count'] >= $row['gioi_han_moi_user']) {
        continue; // Bỏ qua voucher đã hết lượt
    }

    // Tính số lượng còn lại
    $con_lai = null;
    if ($row['so_luong'] !== null) {
        $con_lai = $row['so_luong'] - $row['da_su_dung'];
    }

    // Tính % giảm nếu là loại percent
    $text_giam = '';
    if ($row['loai_giam'] == 'percent') {
        $text_giam = 'Giảm ' . $row['gia_tri_giam'] . '%';
        if ($row['giam_toi_da'] !== null) {
            $text_giam .= ' (tối đa ' . number_format($row['giam_toi_da'], 0, ',', '.') . 'đ)';
        }
    } elseif ($row['loai_giam'] == 'fixed') {
        $text_giam = 'Giảm ' . number_format($row['gia_tri_giam'], 0, ',', '.') . 'đ';
    } elseif ($row['loai_giam'] == 'freeship') {
        $text_giam = 'Miễn phí vận chuyển';
    }

    $voucher_item = [
        'id' => $row['id'],
        'ma_voucher' => $row['ma_voucher'],
        'ten_voucher' => $row['ten_voucher'],
        'mo_ta' => $row['mo_ta'],
        'loai_giam' => $row['loai_giam'],
        'gia_tri_giam' => $row['gia_tri_giam'],
        'giam_toi_da' => $row['giam_toi_da'],
        'don_toi_thieu' => $row['don_toi_thieu'],
        'text_giam' => $text_giam,
        'text_dieu_kien' => 'Đơn tối thiểu ' . number_format($row['don_toi_thieu'], 0, ',', '.') . 'đ',
        'con_lai' => $con_lai,
        'ngay_het_han' => $row['ngay_het_han'],
        'ap_dung_cho' => $row['ap_dung_cho'],
        'user_used_count' => $row['user_used_count'],
        'gioi_han_moi_user' => $row['gioi_han_moi_user'],
        'con_luot' => $row['gioi_han_moi_user'] - $row['user_used_count']
    ];

    // Phân loại voucher có thể dùng ngay hay chưa
    if ($tong_tien > 0) {
        if ($tong_tien >= $row['don_toi_thieu']) {
            $voucher_item['co_the_dung'] = true;
            $vouchers_applicable[] = $voucher_item;
        } else {
            $voucher_item['co_the_dung'] = false;
            $voucher_item['thieu'] = $row['don_toi_thieu'] - $tong_tien;
            $vouchers_not_applicable[] = $voucher_item;
        }
    } else {
        // Nếu không truyền tổng tiền, trả về tất cả
        $vouchers[] = $voucher_item;
    }
}

// Trả về kết quả
if ($tong_tien > 0) {
    echo json_encode([
        'success' => true,
        'message' => 'Lấy danh sách voucher thành công',
        'is_new_user' => $is_new_user,
        'tong_tien' => $tong_tien,
        'vouchers_applicable' => $vouchers_applicable,
        'vouchers_not_applicable' => $vouchers_not_applicable,
        'total_applicable' => count($vouchers_applicable),
        'total_not_applicable' => count($vouchers_not_applicable)
    ]);
} else {
    echo json_encode([
        'success' => true,
        'message' => 'Lấy danh sách voucher thành công',
        'is_new_user' => $is_new_user,
        'vouchers' => $vouchers,
        'total' => count($vouchers)
    ]);
}

$conn->close();
?>

