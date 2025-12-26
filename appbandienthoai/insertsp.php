<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: application/json; charset=utf-8');
include "connect.php";


$tensp   = trim($_POST['tensp'] ?? '');
$giasp   = trim($_POST['giasp'] ?? ''); // may contain thousand separators or text
$hinhanh = trim($_POST['hinhanh'] ?? '');
$mota    = trim($_POST['mota'] ?? '');
$loai    = intval($_POST['loai'] ?? 0);
$soluong = intval($_POST['soluongtonkho'] ?? 0);

// Basic normalization for price: remove common thousand separators and non-numeric chars,
// then cast to float. This avoids inserting strings into numeric DB columns.
$giasp_clean = preg_replace('/[^0-9\.\-]/u', '', str_replace(',', '.', str_replace(' ', '', $giasp)));
if ($giasp_clean === '' || stripos($giasp, 'tìm kiếm') !== false) {
    $giasp_num = 0.0;
} else {
    $giasp_num = (float) $giasp_clean;
}

if ($tensp === '' || $loai <= 0) {
    echo json_encode(['success' => false, 'message' => 'Thiếu dữ liệu bắt buộc'], JSON_UNESCAPED_UNICODE);
    exit;
}

// Use 'd' for double/float for price. Order: tensp (s), giasp (d), hinhanh (s), mota (s), loai (i), soluong (i)
$query = "INSERT INTO sanphammoi (tensp, giasp, hinhanh, mota, loai, soluongtonkho)\n          VALUES (?, ?, ?, ?, ?, ?)";
$stmt = mysqli_prepare($conn, $query);
if (! $stmt) {
    echo json_encode(['success' => false, 'message' => 'Prepare failed: ' . mysqli_error($conn)], JSON_UNESCAPED_UNICODE);
    exit;
}

// bind_param returns false on failure
if (! mysqli_stmt_bind_param($stmt, 'sdssii', $tensp, $giasp_num, $hinhanh, $mota, $loai, $soluong)) {
    echo json_encode(['success' => false, 'message' => 'Bind failed: ' . mysqli_stmt_error($stmt)], JSON_UNESCAPED_UNICODE);
    mysqli_stmt_close($stmt);
    exit;
}

$ok = mysqli_stmt_execute($stmt);

if ($ok) {
    echo json_encode(['success' => true, 'message' => 'Thêm sản phẩm thành công'], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(['success' => false, 'message' => 'Lỗi: ' . mysqli_error($conn)], JSON_UNESCAPED_UNICODE);
}

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>
