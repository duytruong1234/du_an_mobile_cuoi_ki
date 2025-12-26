<?php
include "connect.php";

header('Content-Type: application/json; charset=utf-8');

// Query từ bảng loaisp để lấy danh sách loại sản phẩm
$query = "SELECT id, tensanpham, hinhanh FROM loaisp ORDER BY id ASC";
$data = mysqli_query($conn, $query);

if (!$data) {
    echo json_encode([
        'success' => false,
        'message' => 'Lỗi truy vấn: ' . mysqli_error($conn),
        'result' => []
    ]);
    exit;
}

$result = array();
while ($row = mysqli_fetch_assoc($data)) {
    $result[] = $row;
}

if (!empty($result)) {
    $arr = [
        'success' => true,
        'message' => 'Thành công',
        'result' => $result
    ];
} else {
    $arr = [
        'success' => false,
        'message' => 'Không có dữ liệu loại sản phẩm',
        'result' => []
    ];
}

echo json_encode($arr);
?>
