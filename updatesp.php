<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'connect.php'; // Assuming connect.php exists with database connection

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$tensp = isset($_POST['tensp']) ? $_POST['tensp'] : '';
$giasp = isset($_POST['giasp']) ? $_POST['giasp'] : '';
$hinhanh = isset($_POST['hinhanh']) ? $_POST['hinhanh'] : '';
$mota = isset($_POST['mota']) ? $_POST['mota'] : '';
$id = isset($_POST['id']) ? (int)$_POST['id'] : 0;
$loai = isset($_POST['loai']) ? (int)$_POST['loai'] : 0;

if (empty($tensp) || empty($giasp) || empty($hinhanh) || empty($mota) || $id <= 0 || $loai <= 0) {
    echo json_encode(['success' => false, 'message' => 'Thiếu thông tin cần thiết']);
    exit;
}

try {
    $stmt = $conn->prepare("UPDATE sanpham SET tensp = ?, giasp = ?, hinhanh = ?, mota = ?, loai = ? WHERE id = ?");
    $stmt->bind_param("ssssii", $tensp, $giasp, $hinhanh, $mota, $loai, $id);

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode(['success' => true, 'message' => 'Cập nhật thành công']);
        } else {
            echo json_encode(['success' => false, 'message' => 'Không tìm thấy sản phẩm để cập nhật']);
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Cập nhật thất bại: ' . $stmt->error]);
    }

    $stmt->close();
} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()]);
}

$conn->close();
?>
