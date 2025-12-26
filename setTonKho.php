<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'connect.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $idsp = isset($_POST['idsp']) ? intval($_POST['idsp']) : 0;
    $soluong = isset($_POST['soluong']) ? intval($_POST['soluong']) : null;

    if ($idsp == 0 || $soluong === null || $soluong < 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Dữ liệu không hợp lệ'
        ]);
        exit;
    }

    // Update absolute stock
    $sql = "UPDATE sanphammoi SET soluongtonkho = $soluong WHERE id = $idsp";
    if (mysqli_query($conn, $sql)) {
        $result = mysqli_query($conn, "SELECT soluongtonkho FROM sanphammoi WHERE id = $idsp");
        $row = mysqli_fetch_assoc($result);
        echo json_encode([
            'success' => true,
            'message' => 'Đặt số lượng tồn kho thành công',
            'soluongtonkho' => intval($row['soluongtonkho'])
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Lỗi cập nhật: ' . mysqli_error($conn)
        ]);
    }

    mysqli_close($conn);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Phương thức không hợp lệ'
    ]);
}
?>
