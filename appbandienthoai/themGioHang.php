<?php
include "connect.php";

// API Thêm sản phẩm vào giỏ hàng
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $iduser = isset($_POST['iduser']) ? intval($_POST['iduser']) : 0;
    $idsp = isset($_POST['idsp']) ? intval($_POST['idsp']) : 0;
    $tensp = isset($_POST['tensp']) ? mysqli_real_escape_string($conn, $_POST['tensp']) : '';
    $giasp = isset($_POST['giasp']) ? intval($_POST['giasp']) : 0;
    $hinhsp = isset($_POST['hinhsp']) ? mysqli_real_escape_string($conn, $_POST['hinhsp']) : '';
    $soluong = isset($_POST['soluong']) ? intval($_POST['soluong']) : 1;

    // Validate input
    if ($iduser == 0 || $idsp == 0 || empty($tensp) || $giasp == 0) {
        echo json_encode(array(
            'success' => false,
            'message' => 'Dữ liệu không hợp lệ'
        ));
        exit;
    }

    // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
    $check_sql = "SELECT id, soluong FROM giohang WHERE iduser = $iduser AND idsp = $idsp";
    $result = mysqli_query($conn, $check_sql);

    if (mysqli_num_rows($result) > 0) {
        // Sản phẩm đã có, cập nhật số lượng
        $row = mysqli_fetch_assoc($result);
        $soluong_moi = $row['soluong'] + $soluong;

        $update_sql = "UPDATE giohang SET soluong = $soluong_moi, ngaycapnhat = NOW() WHERE id = " . $row['id'];

        if (mysqli_query($conn, $update_sql)) {
            echo json_encode(array(
                'success' => true,
                'message' => 'Đã cập nhật số lượng sản phẩm trong giỏ hàng',
                'action' => 'updated',
                'soluong' => $soluong_moi
            ));
        } else {
            echo json_encode(array(
                'success' => false,
                'message' => 'Lỗi cập nhật: ' . mysqli_error($conn)
            ));
        }
    } else {
        // Sản phẩm chưa có, thêm mới
        $insert_sql = "INSERT INTO giohang (iduser, idsp, tensp, giasp, hinhsp, soluong)
                       VALUES ($iduser, $idsp, '$tensp', $giasp, '$hinhsp', $soluong)";

        if (mysqli_query($conn, $insert_sql)) {
            echo json_encode(array(
                'success' => true,
                'message' => 'Đã thêm sản phẩm vào giỏ hàng',
                'action' => 'inserted',
                'id' => mysqli_insert_id($conn)
            ));
        } else {
            echo json_encode(array(
                'success' => false,
                'message' => 'Lỗi thêm mới: ' . mysqli_error($conn)
            ));
        }
    }
} else {
    echo json_encode(array(
        'success' => false,
        'message' => 'Method không hợp lệ'
    ));
}

mysqli_close($conn);
?>

