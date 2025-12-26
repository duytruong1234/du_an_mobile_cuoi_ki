<?php
include "connect.php";
header('Content-Type: application/json; charset=utf-8');

try {
    // Nhan tham so loc tu request
    $status = isset($_POST['status']) ? $_POST['status'] : 'all';
    $loai_giam = isset($_POST['loai_giam']) ? $_POST['loai_giam'] : 'all';
    $search = isset($_POST['search']) ? trim($_POST['search']) : '';

    // Bat dau query
    $sql = "SELECT * FROM voucher WHERE 1=1";
    $params = array();
    $types = "";

    // Loc theo trang thai
    if ($status !== 'all') {
        if ($status === 'active') {
            $sql .= " AND trang_thai = 1 AND ngay_het_han > NOW()";
        } elseif ($status === 'expired') {
            $sql .= " AND ngay_het_han <= NOW()";
        } elseif ($status === 'disabled') {
            $sql .= " AND trang_thai = 0";
        }
    }

    // Loc theo loai giam
    if ($loai_giam !== 'all') {
        $sql .= " AND loai_giam = ?";
        $params[] = $loai_giam;
        $types .= "s";
    }

    // Tim kiem theo ma hoac ten voucher
    if (!empty($search)) {
        $sql .= " AND (ma_voucher LIKE ? OR ten_voucher LIKE ?)";
        $searchParam = '%' . $search . '%';
        $params[] = $searchParam;
        $params[] = $searchParam;
        $types .= "ss";
    }

    // Sap xep theo ngay tao moi nhat
    $sql .= " ORDER BY ngay_tao DESC";

    // Chuan bi statement
    $stmt = $conn->prepare($sql);

    if ($stmt === false) {
        throw new Exception("Prepare failed: " . $conn->error);
    }

    // Bind parameters neu co
    if (!empty($params)) {
        $stmt->bind_param($types, ...$params);
    }

    // Thuc thi query
    $stmt->execute();
    $result = $stmt->get_result();

    // Lay tat ca voucher
    $vouchers = array();
    while ($row = $result->fetch_assoc()) {
        $vouchers[] = array(
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
            'ngay_tao' => $row['ngay_tao'],
            'con_lai' => $row['so_luong'] ? ((int)$row['so_luong'] - (int)$row['da_su_dung']) : null,
            'text_giam' => getTextGiam($row),
            'text_dieu_kien' => getTextDieuKien($row),
            'is_expired' => (strtotime($row['ngay_het_han']) <= time()),
            'is_active' => ((int)$row['trang_thai'] === 1 && strtotime($row['ngay_het_han']) > time())
        );
    }

    // Tra ve ket qua
    $response = array(
        'success' => true,
        'message' => 'Lay danh sach voucher thanh cong',
        'vouchers' => $vouchers,
        'total' => count($vouchers)
    );

    echo json_encode($response, JSON_UNESCAPED_UNICODE);

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    $error_response = array(
        'success' => false,
        'message' => 'Loi: ' . $e->getMessage(),
        'vouchers' => array(),
        'total' => 0
    );
    echo json_encode($error_response, JSON_UNESCAPED_UNICODE);
}

function getTextGiam($voucher) {
    if ($voucher['loai_giam'] === 'percent') {
        $text = "Giam " . $voucher['gia_tri_giam'] . "%";
        if ($voucher['giam_toi_da']) {
            $text .= " (toi da " . number_format($voucher['giam_toi_da'], 0, ',', '.') . "d)";
        }
        return $text;
    } elseif ($voucher['loai_giam'] === 'fixed') {
        return "Giam " . number_format($voucher['gia_tri_giam'], 0, ',', '.') . "d";
    } elseif ($voucher['loai_giam'] === 'freeship') {
        return "Mien phi van chuyen";
    }
    return "";
}

function getTextDieuKien($voucher) {
    if ($voucher['don_toi_thieu'] > 0) {
        return "Don toi thieu " . number_format($voucher['don_toi_thieu'], 0, ',', '.') . "d";
    }
    return "Khong co dieu kien";
}
?>

