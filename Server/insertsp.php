<?php
header('Content-Type: application/json; charset=utf-8');
include "connect.php";

// Debug log incoming POST and raw input
$logPath = __DIR__ . '/insertsp_debug.log';
$logEntry = date('c') . " POST=" . json_encode($_POST) . " RAW=" . file_get_contents('php://input') . PHP_EOL;
file_put_contents($logPath, $logEntry, FILE_APPEND);

// Read and sanitize inputs
$tensp = isset($_POST['tensp']) ? trim($_POST['tensp']) : '';
$gia = isset($_POST['gia']) ? trim($_POST['gia']) : '';
$hinhanh = isset($_POST['hinhanh']) ? trim($_POST['hinhanh']) : '';
$mota = isset($_POST['mota']) ? trim($_POST['mota']) : '';
$loai = isset($_POST['loai']) ? intval($_POST['loai']) : 0;
// Support multiple possible field names for stock coming from different clients
$soluong = 0;
if (isset($_POST['soluong'])) {
    $soluong = intval($_POST['soluong']);
} elseif (isset($_POST['soluongtonkho'])) {
    $soluong = intval($_POST['soluongtonkho']);
} elseif (isset($_POST['soluong_tonkho'])) {
    $soluong = intval($_POST['soluong_tonkho']);
}

// Basic validation
if ($tensp === '' || $gia === '' || $loai <= 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Thiếu dữ liệu bắt buộc (tensp, gia, loai)'
    ]);
    exit;
}

// Ensure soluong is non-negative
if ($soluong < 0) $soluong = 0;

// Log normalized stock value for debugging
file_put_contents($logPath, date('c') . " PARSED_STOCK=" . $soluong . PHP_EOL, FILE_APPEND);

// Prepared statement insert
$stmt = mysqli_prepare($conn, "INSERT INTO sanphammoi (`tensp`, `giasp`, `hinhanh`, `mota`, `loai`, `soluongtonkho`) VALUES (?, ?, ?, ?, ?, ?)");
if (!$stmt) {
    file_put_contents($logPath, date('c') . " PREPARE_FAILED=" . mysqli_error($conn) . PHP_EOL, FILE_APPEND);
    echo json_encode(['success' => false, 'message' => 'Prepare failed: ' . mysqli_error($conn)]);
    exit;
}

mysqli_stmt_bind_param($stmt, 'ssssii', $tensp, $gia, $hinhanh, $mota, $loai, $soluong);
$exec = mysqli_stmt_execute($stmt);
if (!$exec) {
    $err = mysqli_stmt_error($stmt);
    mysqli_stmt_close($stmt);
    file_put_contents($logPath, date('c') . " EXEC_FAILED=" . $err . PHP_EOL, FILE_APPEND);
    echo json_encode(['success' => false, 'message' => 'Insert failed: ' . $err]);
    exit;
}

$inserted_id = mysqli_insert_id($conn);
mysqli_stmt_close($stmt);

// Fetch the newly inserted row to return (including soluongtonkho)
$sel = mysqli_prepare($conn, "SELECT id, tensp, giasp, hinhanh, mota, loai, soluongtonkho FROM sanphammoi WHERE id = ? LIMIT 1");
if ($sel) {
    mysqli_stmt_bind_param($sel, 'i', $inserted_id);
    mysqli_stmt_execute($sel);
    $res = mysqli_stmt_get_result($sel);
    $row = $res ? mysqli_fetch_assoc($res) : null;
    mysqli_stmt_close($sel);

    if ($row) {
        // Log the fetched row
        file_put_contents($logPath, date('c') . " INSERTED_ROW=" . json_encode($row) . PHP_EOL, FILE_APPEND);
        echo json_encode(['success' => true, 'message' => 'Thêm sản phẩm thành công', 'data' => $row]);
    } else {
        file_put_contents($logPath, date('c') . " INSERTED_ID_ONLY=" . $inserted_id . PHP_EOL, FILE_APPEND);
        echo json_encode(['success' => true, 'message' => 'Thêm sản phẩm thành công nhưng không thể lấy bản ghi', 'id' => $inserted_id]);
    }
} else {
    // Could not prepare select, but insertion succeeded
    file_put_contents($logPath, date('c') . " INSERTED_ID_ONLY=" . $inserted_id . PHP_EOL, FILE_APPEND);
    echo json_encode(['success' => true, 'message' => 'Thêm sản phẩm thành công', 'id' => $inserted_id]);
}

mysqli_close($conn);
?>
