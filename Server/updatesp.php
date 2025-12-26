<?php
include "connect.php";
header('Content-Type: application/json');

// Read and sanitize inputs
$tensp = mysqli_real_escape_string($conn, isset($_POST['tensp']) ? $_POST['tensp'] : '');
$giasp = mysqli_real_escape_string($conn, isset($_POST['giasp']) ? $_POST['giasp'] : '');
$hinhanh = mysqli_real_escape_string($conn, isset($_POST['hinhanh']) ? $_POST['hinhanh'] : '');
$mota = mysqli_real_escape_string($conn, isset($_POST['mota']) ? $_POST['mota'] : '');
$loai = isset($_POST['loai']) ? (int)$_POST['loai'] : 0;
$id = isset($_POST['id']) ? (int)$_POST['id'] : 0;
// Accept stock field; support multiple possible names for compatibility
$soluongtonkho = 0;
if (isset($_POST['soluongtonkho'])) {
    $soluongtonkho = (int)$_POST['soluongtonkho'];
} elseif (isset($_POST['soluong'])) {
    $soluongtonkho = (int)$_POST['soluong'];
}

// Build UPDATE query: include soluongtonkho only if id > 0
if ($id <= 0) {
    echo json_encode(['success' => false, 'message' => 'ID không hợp lệ']);
    exit;
}

// Prepare update statement with soluongtonkho
$query = "UPDATE sanphammoi SET tensp='$tensp', giasp='$giasp', hinhanh='$hinhanh', mota='$mota', loai=$loai, soluongtonkho=$soluongtonkho WHERE id=$id";

if (mysqli_query($conn, $query) && mysqli_affected_rows($conn) > 0) {
    $arr = [
        'success' => true,
        'message' => "Cập nhật thành công"
    ];
} else {
    $arr = [
        'success' => false,
        'message' => "Cập nhật thất bại: " . mysqli_error($conn)
    ];
}

echo json_encode($arr);
?>