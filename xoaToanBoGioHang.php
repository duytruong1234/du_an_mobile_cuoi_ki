<?php
include "connect.php";

// API Xóa toàn bộ giỏ hàng của user
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;

    if ($iduser == 0) {
        echo json_encode(array(
            'success' => false,
            'message' => 'ID user không hợp lệ'
        ));
        exit;
    }

    $sql = "DELETE FROM giohang WHERE iduser = $iduser";

    if (mysqli_query($conn, $sql)) {
        echo json_encode(array(
            'success' => true,
            'message' => 'Đã xóa toàn bộ giỏ hàng',
            'deleted_count' => mysqli_affected_rows($conn)
        ));
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

