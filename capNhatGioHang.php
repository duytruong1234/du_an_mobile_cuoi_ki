<?php
include "connect.php";

// API Cập nhật số lượng sản phẩm trong giỏ hàng
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
    $idsp = isset($_POST['idsp']) ? intval($_POST['idsp']) : 0;
    $soluong = isset($_POST['soluong']) ? intval($_POST['soluong']) : 0;

    if ($iduser == 0 || $idsp == 0 || $soluong < 0) {
        echo json_encode(array(
            'success' => false,
            'message' => 'Dữ liệu không hợp lệ'
        ));
        exit;
    }

    // Nếu số lượng = 0, xóa sản phẩm
    if ($soluong == 0) {
        $sql = "DELETE FROM giohang WHERE iduser = $iduser AND idsp = $idsp";
    } else {
        $sql = "UPDATE giohang SET soluong = $soluong, ngaycapnhat = NOW()
                WHERE iduser = $iduser AND idsp = $idsp";
    }

    if (mysqli_query($conn, $sql)) {
        if (mysqli_affected_rows($conn) > 0) {
            echo json_encode(array(
                'success' => true,
                'message' => $soluong == 0 ? 'Đã xóa sản phẩm' : 'Đã cập nhật số lượng',
                'soluong' => $soluong
            ));
        } else {
            echo json_encode(array(
                'success' => false,
                'message' => 'Không có thay đổi hoặc sản phẩm không tồn tại'
            ));
        }
    } else {
        echo json_encode(array(
            'success' => false,
            'message' => 'Lỗi cập nhật: ' . mysqli_error($conn)
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

