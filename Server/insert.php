<?php
// Safe, corrected insert endpoint for sanphammoi
// - Ensures proper types for prepared statement binds
// - Accepts `soluong` or `soluongtonkho` POST field
// - Returns JSON with inserted record on success
header('Content-Type: application/json; charset=utf-8');
include "connect.php";

// Read raw POST data for logging (optional)
//$logPath = __DIR__ . '/insert_debug.log';
//file_put_contents($logPath, date('c') . " POST=" . json_encode($_POST) . "\n", FILE_APPEND);

// Read and sanitize inputs
$tensp   = isset($_POST['tensp']) ? trim($_POST['tensp']) : '';
$gia     = isset($_POST['gia']) ? trim($_POST['gia']) : '';
hinhanh  = isset($_POST['hinhanh']) ? trim($_POST['hinhanh']) : '';
$mota    = isset($_POST['mota']) ? trim($_POST['mota']) : '';
$loai    = isset($_POST['loai']) ? intval($_POST['loai']) : 0;
// accept either 'soluong' or legacy 'soluongtonkho'
$soluong = isset($_POST['soluong']) ? intval($_POST['soluong']) : (isset($_POST['soluongtonkho']) ? intval($_POST['soluongtonkho']) : 0);

// Normalize gia: if empty or contains 'tìm kiếm' set to 0
if ($gia === '' || (stripos($gia, 'tìm kiếm') !== false)) {
    $gia = '0';
}

// Basic validation
if ($tensp === '' || $gia === '' || $loai <= 0) {
    echo json_encode(['success' => false, 'message' => 'Thiếu dữ liệu bắt buộc (tensp, gia, loai)'], JSON_UNESCAPED_UNICODE);
    mysqli_close($conn);
    exit;
}

// Prepare and execute insert with correct bind types
$query = "INSERT INTO sanphammoi (tensp, giasp, hinhanh, mota, loai, soluongtonkho) VALUES (?, ?, ?, ?, ?, ?)";
$stmt = mysqli_prepare($conn, $query);
if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'Prepare failed: ' . mysqli_error($conn)], JSON_UNESCAPED_UNICODE);
    mysqli_close($conn);
    exit;
}
// Bind types: s (string) for tensp,giasp,hinhanh,mota; i (int) for loai and soluong
mysqli_stmt_bind_param($stmt, 'ssssii', $tensp, $gia, $hinhanh, $mota, $loai, $soluong);
$ok = mysqli_stmt_execute($stmt);
if (!$ok) {
    $err = mysqli_stmt_error($stmt);
    mysqli_stmt_close($stmt);
    echo json_encode(['success' => false, 'message' => 'Insert failed: ' . $err], JSON_UNESCAPED_UNICODE);
    mysqli_close($conn);
    exit;
}

$inserted_id = mysqli_insert_id($conn);
mysqli_stmt_close($stmt);

// Try to fetch the inserted row to return to client
$sel = mysqli_prepare($conn, "SELECT id, tensp, giasp, hinhanh, mota, loai, soluongtonkho FROM sanphammoi WHERE id = ? LIMIT 1");
if ($sel) {
    mysqli_stmt_bind_param($sel, 'i', $inserted_id);
    mysqli_stmt_execute($sel);
    $res = mysqli_stmt_get_result($sel);
    $row = $res ? mysqli_fetch_assoc($res) : null;
    mysqli_stmt_close($sel);
    if ($row) {
        echo json_encode(['success' => true, 'message' => 'Thêm sản phẩm thành công', 'data' => $row], JSON_UNESCAPED_UNICODE);
        mysqli_close($conn);
        exit;
    }
}

// Fallback success response
echo json_encode(['success' => true, 'message' => 'Thêm sản phẩm thành công', 'id' => $inserted_id], JSON_UNESCAPED_UNICODE);
mysqli_close($conn);
?>
