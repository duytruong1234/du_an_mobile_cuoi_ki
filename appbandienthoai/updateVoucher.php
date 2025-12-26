<?php
/**
 * API CẬP NHẬT VOUCHER
 * Endpoint: updateVoucher.php
 * Method: POST
 *
 * Params:
 * - id: ID voucher cần cập nhật
 * - role: Role của user (1 = admin)
 * - ma_voucher: Mã voucher (unique)
 * - ten_voucher: Tên voucher
 * - mo_ta: Mô tả
 * - loai_giam: percent/fixed/freeship
 * - gia_tri_giam: Giá trị giảm
 * - giam_toi_da: Giảm tối đa (nullable)
 * - don_toi_thieu: Đơn tối thiểu
 * - ap_dung_cho: all/new_user/old_user/first_order
 * - so_luong: Số lượng voucher (nullable)
 * - gioi_han_moi_user: Giới hạn mỗi user
 * - ngay_bat_dau: Ngày bắt đầu
 * - ngay_het_han: Ngày hết hạn
 * - trang_thai: 0/1
 */

error_reporting(0);
ini_set('display_errors', 0);

include "connect.php";
header('Content-Type: application/json; charset=utf-8');

// Kiểm tra method POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed'
    ]);
    exit;
}

// Kiểm tra role
if (!isset($_POST['role']) || intval($_POST['role']) != 1) {
    echo json_encode([
        'success' => false,
        'message' => 'Không có quyền sửa voucher. Chỉ admin mới có quyền này.'
    ]);
    exit;
}

// Kiểm tra ID voucher
if (!isset($_POST['id']) || intval($_POST['id']) <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'ID voucher không hợp lệ'
    ]);
    exit;
}

$id = intval($_POST['id']);

// Validate dữ liệu bắt buộc
$required_fields = [
    'ma_voucher' => 'Mã voucher',
    'ten_voucher' => 'Tên voucher',
    'loai_giam' => 'Loại giảm',
    'gia_tri_giam' => 'Giá trị giảm',
    'don_toi_thieu' => 'Đơn tối thiểu',
    'ap_dung_cho' => 'Áp dụng cho',
    'gioi_han_moi_user' => 'Giới hạn mỗi user',
    'ngay_bat_dau' => 'Ngày bắt đầu',
    'ngay_het_han' => 'Ngày hết hạn',
    'trang_thai' => 'Trạng thái'
];

foreach ($required_fields as $field => $name) {
    if (!isset($_POST[$field]) || trim($_POST[$field]) === '') {
        echo json_encode([
            'success' => false,
            'message' => "Thiếu thông tin: $name"
        ]);
        exit;
    }
}

// Lấy dữ liệu từ POST
$ma_voucher = strtoupper(trim($_POST['ma_voucher']));
$ten_voucher = trim($_POST['ten_voucher']);
$mo_ta = isset($_POST['mo_ta']) ? trim($_POST['mo_ta']) : '';
$loai_giam = trim($_POST['loai_giam']);
$gia_tri_giam = floatval($_POST['gia_tri_giam']);
$giam_toi_da = isset($_POST['giam_toi_da']) && $_POST['giam_toi_da'] !== '' ? floatval($_POST['giam_toi_da']) : null;
$don_toi_thieu = floatval($_POST['don_toi_thieu']);
$ap_dung_cho = trim($_POST['ap_dung_cho']);
$so_luong = isset($_POST['so_luong']) && $_POST['so_luong'] !== '' ? intval($_POST['so_luong']) : null;
$gioi_han_moi_user = intval($_POST['gioi_han_moi_user']);
$ngay_bat_dau = trim($_POST['ngay_bat_dau']);
$ngay_het_han = trim($_POST['ngay_het_han']);
$trang_thai = intval($_POST['trang_thai']);

// Kiểm tra voucher có tồn tại không
$check_query = "SELECT ma_voucher FROM voucher WHERE id = ?";
$check_stmt = $conn->prepare($check_query);
$check_stmt->bind_param("i", $id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows == 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Voucher không tồn tại'
    ]);
    exit;
}

// Kiểm tra mã voucher có trùng với voucher khác không
$check_duplicate_query = "SELECT id FROM voucher WHERE ma_voucher = ? AND id != ?";
$check_dup_stmt = $conn->prepare($check_duplicate_query);
$check_dup_stmt->bind_param("si", $ma_voucher, $id);
$check_dup_stmt->execute();
$check_dup_result = $check_dup_stmt->get_result();

if ($check_dup_result->num_rows > 0) {
    echo json_encode([
        'success' => false,
        'message' => "Mã voucher '$ma_voucher' đã được sử dụng bởi voucher khác"
    ]);
    exit;
}

// Validate loại giảm
$allowed_loai_giam = ['percent', 'fixed', 'freeship'];
if (!in_array($loai_giam, $allowed_loai_giam)) {
    echo json_encode([
        'success' => false,
        'message' => 'Loại giảm không hợp lệ'
    ]);
    exit;
}

// Validate áp dụng cho
$allowed_ap_dung = ['all', 'new_user', 'old_user', 'first_order'];
if (!in_array($ap_dung_cho, $allowed_ap_dung)) {
    echo json_encode([
        'success' => false,
        'message' => 'Áp dụng cho không hợp lệ'
    ]);
    exit;
}

// Validate giá trị giảm
if ($loai_giam == 'percent' && ($gia_tri_giam <= 0 || $gia_tri_giam > 100)) {
    echo json_encode([
        'success' => false,
        'message' => 'Giá trị giảm phần trăm phải từ 0-100%'
    ]);
    exit;
}

if ($gia_tri_giam < 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Giá trị giảm không hợp lệ'
    ]);
    exit;
}

// Update voucher
$query = "UPDATE voucher SET
    ma_voucher = ?,
    ten_voucher = ?,
    mo_ta = ?,
    loai_giam = ?,
    gia_tri_giam = ?,
    giam_toi_da = ?,
    don_toi_thieu = ?,
    ap_dung_cho = ?,
    so_luong = ?,
    gioi_han_moi_user = ?,
    ngay_bat_dau = ?,
    ngay_het_han = ?,
    trang_thai = ?
    WHERE id = ?";

$stmt = $conn->prepare($query);
$stmt->bind_param(
    "ssssddssiissii",
    $ma_voucher,
    $ten_voucher,
    $mo_ta,
    $loai_giam,
    $gia_tri_giam,
    $giam_toi_da,
    $don_toi_thieu,
    $ap_dung_cho,
    $so_luong,
    $gioi_han_moi_user,
    $ngay_bat_dau,
    $ngay_het_han,
    $trang_thai,
    $id
);

if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => "Cập nhật voucher '$ma_voucher' thành công"
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi: ' . $stmt->error
    ]);
}

$stmt->close();
$conn->close();
?>

