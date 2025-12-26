<?php
include "connect.php";
header('Content-Type: application/json; charset=utf-8');

try {
    // Nhận tham số lọc từ request
    $status = isset($_POST['status']) ? $_POST['status'] : 'all';
    $loai_giam = isset($_POST['loai_giam']) ? $_POST['loai_giam'] : 'all';
    $search = isset($_POST['search']) ? trim($_POST['search']) : '';

    // Bắt đầu query
    $sql = "SELECT * FROM voucher WHERE 1=1";
    $params = [];
    $types = "";

    // Lọc theo trạng thái
    if ($status !== 'all') {
        if ($status === 'active') {
            $sql .= " AND trang_thai = 1 AND ngay_het_han > NOW() AND ngay_bat_dau <= NOW()";
        } elseif ($status === 'expired') {
            $sql .= " AND ngay_het_han <= NOW()";
        } elseif ($status === 'disabled') {
            $sql .= " AND trang_thai = 0";
        }
    }

    // Lọc theo loại giảm
    if ($loai_giam !== 'all') {
        $sql .= " AND loai_giam = ?";
        $params[] = $loai_giam;
        $types .= "s";
    }

    // Tìm kiếm theo mã hoặc tên voucher
    if (!empty($search)) {
        $sql .= " AND (ma_voucher LIKE ? OR ten_voucher LIKE ?)";
        $searchParam = '%' . $search . '%';
        $params[] = $searchParam;
        $params[] = $searchParam;
        $types .= "ss";
    }

    // Sắp xếp theo ngày tạo mới nhất (dùng created_at trong DB)
    $sql .= " ORDER BY created_at DESC";

    // Chuẩn bị statement
    $stmt = $conn->prepare($sql);
    if ($stmt === false) {
        throw new Exception("Prepare failed: " . $conn->error);
    }

    // Bind parameters nếu có
    if (!empty($params)) {
        $stmt->bind_param($types, ...$params);
    }

    // Thực thi query
    $stmt->execute();
    $result = $stmt->get_result();

    // Lấy tất cả voucher
    $vouchers = [];
    while ($row = $result->fetch_assoc()) {
        $vouchers[] = [
            'id' => (int)$row['id'],
            'ma_voucher' => $row['ma_voucher'],
            'ten_voucher' => $row['ten_voucher'],
            'mo_ta' => $row['mo_ta'],
            'loai_giam' => $row['loai_giam'],
            'gia_tri_giam' => (double)$row['gia_tri_giam'],
            'giam_toi_da' => $row['giam_toi_da'] ? (double)$row['giam_toi_da'] : null,
            'don_toi_thieu' => (double)$row['don_toi_thieu'],
            'ap_dung_cho' => $row['ap_dung_cho'],
            'so_luong' => $row['so_luong'] ? (int)$row['so_luong'] : null,
            'da_su_dung' => (int)$row['da_su_dung'],
            'gioi_han_moi_user' => (int)$row['gioi_han_moi_user'],
            'ngay_bat_dau' => $row['ngay_bat_dau'],
            'ngay_het_han' => $row['ngay_het_han'],
            'trang_thai' => (int)$row['trang_thai'],
            'created_at' => $row['created_at'],
            'updated_at' => $row['updated_at'],
            'con_lai' => $row['so_luong'] ? ((int)$row['so_luong'] - (int)$row['da_su_dung']) : null,
            'text_giam' => getTextGiam($row),
            'text_dieu_kien' => getTextDieuKien($row),
            'is_expired' => (strtotime($row['ngay_het_han']) <= time()),
            'is_active' => ((int)$row['trang_thai'] === 1 && strtotime($row['ngay_het_han']) > time() && strtotime($row['ngay_bat_dau']) <= time())
        ];
    }

    // Trả về kết quả
    echo json_encode([
        'success' => true,
        'message' => 'Lấy danh sách voucher thành công',
        'vouchers' => $vouchers,
        'total' => count($vouchers)
    ], JSON_UNESCAPED_UNICODE);

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi: ' . $e->getMessage(),
        'vouchers' => [],
        'total' => 0
    ], JSON_UNESCAPED_UNICODE);
}


function getTextGiam($voucher) {
    if ($voucher['loai_giam'] === 'percent') {
        $text = "Giảm " . $voucher['gia_tri_giam'] . "%";
        if ($voucher['giam_toi_da']) {
            $text .= " (tối đa " . number_format($voucher['giam_toi_da'], 0, ',', '.') . "đ)";
        }
        return $text;
    } elseif ($voucher['loai_giam'] === 'fixed') {
        return "Giảm " . number_format($voucher['gia_tri_giam'], 0, ',', '.') . "đ";
    } elseif ($voucher['loai_giam'] === 'freeship') {
        return "Miễn phí vận chuyển";
    }
    return "";
}

function getTextDieuKien($voucher) {
    if ($voucher['don_toi_thieu'] > 0) {
        return "Đơn tối thiểu " . number_format($voucher['don_toi_thieu'], 0, ',', '.') . "đ";
    }
    return "Không có điều kiện";
}
?>
