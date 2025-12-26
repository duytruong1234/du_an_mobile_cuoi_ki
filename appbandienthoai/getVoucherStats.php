<?php
/**
 * API THỐNG KÊ VOUCHER
 * Endpoint: admin/getVoucherStats.php
 * Method: GET/POST
 *
 * Params:
 * - voucher_id: ID voucher cần xem chi tiết (optional)
 */

include "../connect.php";
header('Content-Type: application/json; charset=utf-8');

// Kiểm tra quyền admin
session_start();
if (!isset($_SESSION['user']) || $_SESSION['user']['role'] != 1) {
    echo json_encode([
        'success' => false,
        'message' => 'Không có quyền truy cập'
    ]);
    exit;
}

$voucher_id = isset($_REQUEST['voucher_id']) ? intval($_REQUEST['voucher_id']) : null;

if ($voucher_id) {
    // Thống kê chi tiết 1 voucher
    $query = "SELECT v.*,
              COUNT(vu.id) as so_lan_su_dung,
              COUNT(DISTINCT vu.user_id) as so_user_da_dung,
              SUM(vu.gia_tri_giam) as tong_tien_giam,
              AVG(vu.gia_tri_giam) as tb_tien_giam,
              SUM(vu.gia_tri_don_hang) as tong_doanh_thu
              FROM voucher v
              LEFT JOIN voucher_usage vu ON v.id = vu.voucher_id
              WHERE v.id = ?
              GROUP BY v.id";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $voucher_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows == 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Voucher không tồn tại'
        ]);
        exit;
    }

    $stats = $result->fetch_assoc();

    // Lấy lịch sử sử dụng gần đây
    $history_query = "SELECT vu.*, u.username, u.email
                      FROM voucher_usage vu
                      JOIN user u ON vu.user_id = u.id
                      WHERE vu.voucher_id = ?
                      ORDER BY vu.ngay_su_dung DESC
                      LIMIT 20";

    $history_stmt = $conn->prepare($history_query);
    $history_stmt->bind_param("i", $voucher_id);
    $history_stmt->execute();
    $history_result = $history_stmt->get_result();

    $history = [];
    while ($row = $history_result->fetch_assoc()) {
        $history[] = $row;
    }

    echo json_encode([
        'success' => true,
        'statistics' => $stats,
        'recent_usage' => $history
    ]);

} else {
    // Tổng quan tất cả voucher
    $query = "SELECT v.id, v.ma_voucher, v.ten_voucher, v.loai_giam,
              COUNT(vu.id) as so_lan_su_dung,
              COUNT(DISTINCT vu.user_id) as so_user_da_dung,
              SUM(vu.gia_tri_giam) as tong_tien_giam,
              SUM(vu.gia_tri_don_hang) as tong_doanh_thu
              FROM voucher v
              LEFT JOIN voucher_usage vu ON v.id = vu.voucher_id
              GROUP BY v.id
              ORDER BY so_lan_su_dung DESC";

    $result = $conn->query($query);

    $vouchers = [];
    $total_discount = 0;
    $total_revenue = 0;
    $total_usage = 0;

    while ($row = $result->fetch_assoc()) {
        $vouchers[] = $row;
        $total_discount += $row['tong_tien_giam'] ?? 0;
        $total_revenue += $row['tong_doanh_thu'] ?? 0;
        $total_usage += $row['so_lan_su_dung'];
    }

    echo json_encode([
        'success' => true,
        'summary' => [
            'total_vouchers' => count($vouchers),
            'total_usage' => $total_usage,
            'total_discount_amount' => $total_discount,
            'total_revenue' => $total_revenue
        ],
        'vouchers' => $vouchers
    ]);
}

$conn->close();
?>

