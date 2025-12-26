<?php
header('Content-Type: application/json; charset=utf-8');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "appbandienthoai";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(['success' => false, 'message' => 'Kết nối database thất bại', 'result' => []]));
}

$conn->set_charset("utf8");

$page = isset($_POST['page']) ? (int)$_POST['page'] : 1;
$loai = isset($_POST['loai']) ? (int)$_POST['loai'] : 1;

$limit = 10;
$offset = ($page - 1) * $limit;

// Sử dụng query trực tiếp vì bind_param không hoạt động tốt với LIMIT/OFFSET
// ĐÃ SỬA: Đổi từ bảng 'sanpham' sang 'sanphammoi' để lấy giá mới nhất
$sql = "SELECT id, tensp, giasp, hinhanh, mota, loai FROM sanphammoi WHERE loai = " . $loai . " ORDER BY id DESC LIMIT " . $limit . " OFFSET " . $offset;
$result = $conn->query($sql);

if (!$result) {
    echo json_encode(['success' => false, 'message' => 'Lỗi truy vấn: ' . $conn->error, 'result' => []]);
    exit;
}

$products = [];
while ($row = $result->fetch_assoc()) {
    $products[] = $row;
}

$conn->close();

if (count($products) > 0) {
    echo json_encode(['success' => true, 'message' => 'Thành công', 'result' => $products]);
} else {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy sản phẩm', 'result' => []]);
}
?>
