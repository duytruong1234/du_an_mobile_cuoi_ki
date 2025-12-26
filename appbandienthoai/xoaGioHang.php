<?php
include "connect.php";

// API Xóa sản phẩm khỏi giỏ hàng
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
    $idsp = isset($_POST['idsp']) ? intval($_POST['idsp']) : 0;

    if ($iduser == 0 || $idsp == 0) {
        echo json_encode(array(
            'success' => false,
            'message' => 'Dữ liệu không hợp lệ'
        ));
        exit;
    }

    $sql = "DELETE FROM giohang WHERE iduser = $iduser AND idsp = $idsp";

    if (mysqli_query($conn, $sql)) {
        if (mysqli_affected_rows($conn) > 0) {
            echo json_encode(array(
                'success' => true,
                'message' => 'Đã xóa sản phẩm khỏi giỏ hàng'
            ));
        } else {
            echo json_encode(array(
                'success' => false,
                'message' => 'Không tìm thấy sản phẩm trong giỏ hàng'
            ));
        }
    } else {
        echo json_encode(array(
            'success' => false,
            'message' => 'Lỗi xóa: ' . mysqli_error($conn)
        ));
    }
} else {
    echo json_encode(array(
        'success' => false,
        'message' => 'Method không hợp lệ'
    ));
}

mysqli_close($conn);
?>

